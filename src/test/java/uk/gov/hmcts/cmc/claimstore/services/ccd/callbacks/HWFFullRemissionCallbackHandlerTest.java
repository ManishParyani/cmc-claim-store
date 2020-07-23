package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HWF Full Remission Rejected Callback Handler")
class HWFFullRemissionCallbackHandlerTest {

    private static final List<Role> ROLES = Collections.singletonList(Role.CASEWORKER);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(CaseEvent.FULL_REMISSION_HWF_REJECTED);

    private static final String AUTHORISATION = "Bearer: aaaa";
    private HWFFullRemissionCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    @Mock
    private DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;

    @BeforeEach
    public void setUp() {
        handler = new HWFFullRemissionCallbackHandler(caseDetailsConverter, deadlineCalculator, caseMapper);
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.FULL_REMISSION_HWF_REJECTED.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    void shouldUpdateInfo() {
        Claim claim = SampleClaim.getClaimWithFullAdmission();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        Map<String, Object> mappedCaseData = new HashMap<>();
        mappedCaseData.put("helpWithFeesNumber", "139999");
        mappedCaseData.put("hwfFeeDetailsSummary", "NOT_QUALIFY_FEE_ASSISTANCE");
        mappedCaseData.put("hwfMandatoryDetails", "Details");
        when(caseDetailsConverter.convertToMap(caseMapper.to(claim))).thenReturn(mappedCaseData);
        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Map<String, Object> data = response.getData();

        assertThat(data).containsEntry("helpWithFeesNumber", "139999")
            .containsEntry("hwfFeeDetailsSummary", "NOT_QUALIFY_FEE_ASSISTANCE")
            .containsEntry("hwfMandatoryDetails", "Details");
    }

    @Test
    void getSupportedRoles() {
        List<Role> roleList = handler.getSupportedRoles();
        assertEquals(ROLES, roleList);
    }

    @Test
    void handledEvents() {
        List<CaseEvent> caseEventList = handler.handledEvents();
        assertEquals(EVENTS, caseEventList);
    }

}