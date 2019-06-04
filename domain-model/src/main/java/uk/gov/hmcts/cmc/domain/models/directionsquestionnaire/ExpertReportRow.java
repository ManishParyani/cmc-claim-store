package uk.gov.hmcts.cmc.domain.models.directionsquestionnaire;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Getter
public class ExpertReportRow extends CollectionId {
    private String expertName;
    private LocalDate expertReportDate;

    @Builder
    public ExpertReportRow(String id, String expertName, LocalDate expertReportDate) {
        super(id);
        this.expertName = expertName;
        this.expertReportDate = expertReportDate;
    }
}