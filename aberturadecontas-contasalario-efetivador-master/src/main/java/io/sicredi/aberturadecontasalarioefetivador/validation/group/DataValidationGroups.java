package io.sicredi.aberturadecontasalarioefetivador.validation.group;

import jakarta.validation.GroupSequence;

@GroupSequence({DataEmFormatoValidoGroup.class, DataPassadaGroup.class})
public interface DataValidationGroups {
}
