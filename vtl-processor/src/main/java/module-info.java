module fr.insee.vtl.processing {

    requires fr.insee.vtl.model;
    requires fr.insee.vtl.parser;

    exports fr.insee.vtl.processing;

    provides fr.insee.vtl.processing.ProcessingEngine with fr.insee.vtl.processing.InMemoryProcessingEngine;

}