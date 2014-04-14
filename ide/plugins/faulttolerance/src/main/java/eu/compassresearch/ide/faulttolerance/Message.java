/**
 * 
 */
package eu.compassresearch.ide.faulttolerance;

import java.util.ResourceBundle;

/**
 * @author Andr&eacute; Didier (<a href=
 *         "mailto:alrd@cin.ufpe.br?Subject=Package eu.compassresearch.ide.faulttolerance, class Message"
 *         >alrd@cin.ufpe.br</a>)
 * 
 */
public enum Message {
	DIVERGENCE_FREE_JOB, SEMIFAIRNESS_JOB, FULL_FAULT_TOLERANCE_JOB, LIMITED_FAULT_TOLERANCE_JOB, CHECKING_PREREQUISITES, FULL_FAULT_TOLERANCE_SUCCESS, LIMITED_FAULT_TOLERANCE_SUCCESS, LIMITED_FAULT_TOLERANCE_ERROR, NO_PROJECT_SELECTED, MARKER_LOCATION, DIVERGENCE_FREE_ERROR, SEMIFAIR_ERROR, DIVERGENCE_FREE_SEMIFAIR_ERROR, FOLDER_NAME, STARTING_FAULT_TOLERANCE_FILES_MANAGEMENT, UNABLE_TO_CREATE_FAULT_TOLERANCE_PROCESSES_FILE, UNABLE_TO_CREATE_FAULT_TOLERANCE_BASE_FILE, UNABLE_TO_CREATE_FAULT_TOLERANCE_FOLDER, BASE_CML_FILE_NAME, CML_PROCESSES_FILE_NAME, LIMIT_PROCESS_NAME, NO_FAULTS_PROCESS_NAME, LAZY_PROCESS_NAME, LAZY_LIMIT_PROCESS_NAME, DIVERGENCE_FREEDOM_PROCESS_NAME, SEMIFAIRNESS_PROCESS_NAME, UNABLE_TO_CREATE_FORMULA_SCRIPT, DIVERGENCE_FREEDOM_FORMULA_SCRIPT_FILE_NAME, SEMIFAIRNESS_FORMULA_SCRIPT_FILE_NAME, FULL_FAULT_TOLERANCE_FORMULA_SCRIPT_FILE_NAME, LIMITED_FAULT_TOLERANCE_FORMULA_SCRIPT_FILE_NAME, FAULT_TOLERANCE_JOB_NAME, FAULT_TOLERANCE_VERIFICATION_TASK_MESSAGE, LAZY_DEADLOCK_CHECK_PROCESS_NAME, LAZY_LIMIT_DEADLOCK_CHECK_PROCESS_NAME, DEFINITIONS_VERIFICATION_TASK_NAME, EXISTING_NEEDED_CHANNELS, EXISTING_NEEDED_CHANSETS, EXISTING_NEEDED_PROCESSES, CHECK_NAMES_TASK, CHANNELS_NOT_FOUND, CHANSETS_NOT_FOUND, PROCESSES_NOT_FOUND, MISSING_DEFINITIONS, UNABLE_TO_FIND_PROJECT_DEFINITIONS, VALUES_NOT_FOUND, EXISTING_NEEDED_VALUES, EXISTING_NEEDED_NAMESETS, NAMESETS_NOT_FOUND, CHANSET_F_TEMPLATE, CHANSET_E_TEMPLATE, CHANSET_H_TEMPLATE, CHANSET_E_NAME, CHANSET_F_NAME, CHANSET_H_NAME, CHANSET_H_RELATED, PROCESS_CHAOS_E_NAME, PROCESS_CHAOS_E_TEMPLATE, LIMIT_PROCESS_TEMPLATE, DIVERGENCE_FREEDOM_PROCESS_TEMPLATE, SEMIFAIRNESS_PROCESS_TEMPLATE, LAZY_DEADLOCK_CHECK_PROCESS_TEMPLATE, LAZY_LIMIT_DEADLOCK_CHECK_PROCESS_TEMPLATE, NO_FAULTS_PROCESS_TEMPLATE, LAZY_PROCESS_TEMPLATE, LAZY_LIMIT_PROCESS_TEMPLATE, EXCEPTION_OCCURRED, CANCELED_BY_USER, FAULT_TOLERANCE_VERIFICATION_THREAD_NAME, CLEAR_GENERATED_CML_FILES_TASK_NAME, CREATE_CML_FILES_TASK_NAME, CREATE_FORMULA_FILES_TASK_NAME, FIND_PARSE_LIST_DEFINITIONS_TASK_NAME, WRITE_FILE, CREATE_FOLDER, VIEW_LTS_LABEL, DIVERGENCE_FREEDOM_TYPE_NAME, SEMIFAIRNESS_TYPE_NAME, FULL_FAULT_TOLERANCE_TYPE_NAME, LIMITED_FAULT_TOLERANCE_TYPE_NAME, FILTER_PARSE_LIST_DEFINITIONS, CLEAR_GENERATED_FILES_TASK_NAME, CLEAR_GENERATED_FORMULA_FILES_TASK_NAME, PREPARE_DEFINITIONS, FILE_PREPARATION_JOB_NAME, CREATE_FORMULA_FILE, MODEL_CHECKING_JOB_NAME, MODEL_CHECKING_PROPERTY_TASK_NAME, DEADLOCK_CHECK, LIVELOCK_CHECK, FILES_CLEANUP_JOB_NAME, FILES_CLEANUP_TASK_NAME, MARKER_UPDATER_JOB_NAME, MARKER_UPDATER_TASK_NAME, CLEAR_MARKERS_JOB_NAME, PROPERTY_NOT_YET_CHECKED, PROPERTY_CHECKED, DEADLOCK_FREEDOM_TYPE_NAME, DEADLOCK_FREEDOM_CHECK_PROCESS_NAME, DEADLOCK_FREEDOM_FORMULA_SCRIPT_FILE_NAME, NOT_DEADLOCK_FREE_SYSTEM, FAULT_TOLERANCE_CLEAR_FIX_DESCRIPTION, FAULT_TOLERANCE_CLEAR_FIX_LABEL, CHANNELS_E_RELATED, CHANNELS_F_RELATED;

	public String format(Object... params) {
		return String.format(
				ResourceBundle.getBundle("Message").getString(this.name()),
				params);
	}
}
