1. Create virtual environment of all following apps using `python 3.10`.
* infy_dx_downloader
* infy_dx_preprocessor
* infy_dx_indexer
* infy_dx_extractor
* infy_dx_postprocessor
* infy_dx_casecreator

2. If below path don't exist create folders:
* ```C:\WorkArea\documentworkbench\data\input\t02```
* ```C:\WorkArea\documentworkbench\data\input\t01```
* ```C:/workarea/documentworkbench/data/output```
* ```C:/workarea/documentworkbench/data/config```
* ```C:/workarea/documentworkbench/data/work```

3. For documentworkbench case, keep input file at t02. e.g.`"C:\WorkArea\documentworkbench\data\input\t02\swnlp.pdf"`

4. Under config folder keep below config files.
* dpp_docwb_index_input_config.json
* dpp_docwb_infy_annual_report_attr_inference_pipeline_input_config.json
* dpp_docwb_infy_annual_report_profile_inference_pipeline_input_config.json
* dpp_docwb_infy_sow_attr_inference_pipeline_input_config.json
* dpp_docwb_infy_sow_profile_inference_pipeline_input_config.json

5. Verify the URL and provide key in config files e.g. azure_read subscription_key in `masterclient.config`.

6. Keep the prompt template files under `C:\WorkArea\documentworkbench\data\config\prompt_templates`  
e.g. 
```
C:\WorkArea\documentworkbench\data\config\prompt_templates\extractor_attribute_prompt.txt
C:\WorkArea\documentworkbench\data\config\prompt_templates\extractor_attribute_prompt_2.txt
C:\WorkArea\documentworkbench\data\config\prompt_templatesprompt_1.txt
C:\WorkArea\documentworkbench\data\config\prompt_templatesprompt_2.txt
```

7. Run this script to create a Document Workbench case.

8. After successful execution, copy the doc_id folder from chunked and encoded folders and paste it in VM. 
Example of path from where to copy:
```
C:\WorkArea\documentworkbench\docwbvectordb\chunked
C:\WorkArea\documentworkbench\docwbvectordb\encoded\openai-text-embedding-ada-002
```
9. Change the copied folder and file permissions in VM.
If you logged to VM in using winscp right cilck and select properties, then
* if folder, set permission to 2755
* if file, set permission to 0644