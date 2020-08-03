package fr.insee.vtl.model;

import java.util.List;

/**
 * <code>Structured</code> is the base interface for representing structured data.
 */
public interface Structured {

    /**
     * Returns the structure associated to the data as a list of structure components.
     *
     * @return The structure associated to the data as a list of structure components.
     */
    List<Dataset.Component> getDataStructure();

}
