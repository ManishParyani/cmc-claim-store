package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.DirectionsQuestionnaire;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.ExpertReportRow;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.UnavailableDate;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

public class SampleDirectionsQuestionnaire {

    public static SampleDirectionsQuestionnaire builder() { return new SampleDirectionsQuestionnaire(); }

    public DirectionsQuestionnaire build() {
    List<UnavailableDate> unavailableDates = asList(UnavailableDate
        .builder()
        .unavailableDate(LocalDate.of(2050,1,1))
        .build());

    List<ExpertReportRow> expertReportRowsData = asList(ExpertReportRow
        .builder()
        .expertName("expert1")
        .expertReportDate(LocalDate.of(2040,1,1))
        .build());

        return DirectionsQuestionnaire.builder()
            .selfWitness(YesNoOption.YES)
            .howManyOtherWitness(1)
            .hearingLocation("London")
            .hearingLocationSlug("London-Court")
            .exceptionalCircumstancesReason("disabled")
            .unavailableDates(unavailableDates)
            .availableDate(LocalDate.of(2050,1,2))
            .languageInterpreted("some language")
            .signLanguageInterpreted("yes")
            .hearingLoop(YesNoOption.NO)
            .disabledAccess(YesNoOption.YES)
            .otherSupportRequired("maybe")
            .expertReportsRows(expertReportRowsData)
            .expertEvidenceToExamine("nothing")
            .reasonForExpertAdvice("for specified reason")
            .build();
    }
}