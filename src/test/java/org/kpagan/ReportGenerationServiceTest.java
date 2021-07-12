package org.kpagan;

import era.project.enums.SSCLibraryFilesEnum;
import era.project.services.ReportGenerationService;
import era.project.ui.application.ssc.view.SSCMappingTable;
import era.project.utils.ReflectionInitializer;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.*;

public class ReportGenerationServiceTest {

    private ReportGenerationService reportGenerationService = new ReportGenerationService();
    private ReflectionInitializer initializer = new ReflectionInitializer();

    private static final String OUTPUT_FILE = "target/Report.pdf";
    private static final String TEMPLATE = "." + SSCLibraryFilesEnum.SSC_MAPPING_TABLES_AE_TYPE_REPORT_NEW_TEMPLATE_PDF.getFileReportTemplate();

    @Test
    public void test() {
        try (FileOutputStream outStream = new FileOutputStream(OUTPUT_FILE)) {
            Set<String> fields = new HashSet<>();
            fields.addAll(Arrays.asList("mappings", "requirement", "values", "requirementDescr", "document", "description", "text", "title", "filename"));
            SSCMappingTable mappingTable = initializer.initialize(SSCMappingTable.class, fields);
            reportGenerationService.generatePdfReportFromTemplate(TEMPLATE, Arrays.asList(mappingTable), new HashMap<>(), outStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
