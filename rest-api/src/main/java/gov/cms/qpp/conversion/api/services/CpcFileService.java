package gov.cms.qpp.conversion.api.services;

import gov.cms.qpp.conversion.api.model.Metadata;
import gov.cms.qpp.conversion.api.model.UnprocessedCpcFileData;
import java.io.InputStream;
import java.util.List;

/**
 * Service interface to handle processing cpc+ files
 */
public interface CpcFileService {
	/**
	 * Retrieves all unprocessed cpc+ metadata
	 *
	 * @return {@link Metadata} extracted as {@link UnprocessedCpcFileData}.
	 */
	List<UnprocessedCpcFileData> getUnprocessedCpcPlusFiles();

	/**
	 * Retrieves the file location id by metadata id and uses it to retrieve the file
	 *
	 * @param fileId {@link Metadata} identifier
	 * @return file returned as an {@link InputStream}
	 */
	InputStream getFileById(String fileId);

	/**
	 *
	 * @param fileId
	 * @return
	 */
	String processFileById(String fileId);
}
