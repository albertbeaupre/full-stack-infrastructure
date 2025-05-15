package infrastructure.net.web.ui.components;

import infrastructure.net.web.ui.Component;
import infrastructure.net.web.ui.DOMUpdateParam;
import infrastructure.net.web.ui.DOMUpdateType;

import java.util.Arrays;
import java.util.Map;

/**
 * Represents a file input component that allows users to upload one or more files.
 * <p>
 * This component supports:
 * <ul>
 *     <li>Restricting file types via {@code accept} attribute</li>
 *     <li>Allowing multiple file selection</li>
 *     <li>Directory selection support through {@code directory} and {@code webkitdirectory}</li>
 *     <li>Assigning a handler to process uploaded file content</li>
 * </ul>
 * Internally queues DOM updates for dispatch to the client-side browser rendering engine.
 *
 * @author Albert Beaupre
 * @since May 15th, 2025
 */
public class FileUploader extends Component {

    private boolean multipleFiles;
    private String[] fileTypes;
    private FileUploadHandler handler;

    /**
     * Constructs a {@code FileUploader} with a flag indicating whether multiple file selection is allowed.
     *
     * @param multipleFiles true if multiple files can be selected
     */
    public FileUploader(boolean multipleFiles) {
        super("input");
        this.multipleFiles = multipleFiles;
    }

    /**
     * Constructs a {@code FileUploader} with multiple file selection disabled.
     */
    public FileUploader() {
        this(false);
    }

    /**
     * Initializes the DOM representation of this component.
     * <p>
     * This sets:
     * <ul>
     *     <li>{@code type="file"}</li>
     *     <li>{@code directory} and {@code webkitdirectory} attributes for folder selection</li>
     *     <li>{@code accept} attribute if specific file types are configured</li>
     *     <li>{@code multiple} attribute if enabled</li>
     * </ul>
     */
    @Override
    protected void create() {
        this.queueForDispatch(DOMUpdateType.SET_TYPE, DOMUpdateParam.TYPE, "file");

        // Enable directory selection for supporting browsers
        this.queueForDispatch(DOMUpdateType.SET_ATTRIBUTE, Map.of(DOMUpdateParam.KEY, "directory", DOMUpdateParam.VALUE, ""));
        this.queueForDispatch(DOMUpdateType.SET_ATTRIBUTE, Map.of(DOMUpdateParam.KEY, "webkitdirectory", DOMUpdateParam.VALUE, ""));

        // Apply file type restrictions if set
        if (fileTypes != null)
            setAcceptFileTypes(this.fileTypes);

        // Apply multiple file setting if enabled
        if (multipleFiles)
            this.setMultipleFiles(true);
    }

    /**
     * Cleanup logic before component is destroyed.
     * <p>
     * Currently unused, but can be overridden for resource cleanup.
     */
    @Override
    protected void destroy() {
        // No specific teardown behavior
    }

    /**
     * Sets accepted file types for the input element using the {@code accept} attribute.
     *
     * @param types array of MIME types or file extensions
     */
    public void setAcceptFileTypes(String... types) {
        this.fileTypes = types;

        // Format to comma-separated string for HTML accept attribute
        String format = Arrays.toString(fileTypes).replaceAll("[\\[\\]]", "");
        this.queueForDispatch(DOMUpdateType.SET_ATTRIBUTE, Map.of(DOMUpdateParam.KEY, "accept", DOMUpdateParam.VALUE, format));
    }

    /**
     * Checks whether multiple file selection is enabled.
     *
     * @return true if multiple file selection is allowed
     */
    public boolean isMultipleFiles() {
        return multipleFiles;
    }

    /**
     * Enables or disables multiple file selection.
     * <p>
     * Updates the DOM with the {@code multiple} attribute accordingly.
     *
     * @param multipleFiles true to allow multiple files
     */
    public void setMultipleFiles(boolean multipleFiles) {
        this.queueForDispatch(DOMUpdateType.SET_ATTRIBUTE, Map.of(
                DOMUpdateParam.KEY, "multiple",
                DOMUpdateParam.VALUE, String.valueOf(multipleFiles)
        ));
        this.multipleFiles = multipleFiles;
    }

    /**
     * Sets a file upload handler that will be triggered when a file is uploaded by the user.
     * <p>
     * This automatically attaches or reattaches a {@code change} event listener to the input.
     *
     * @param handler the {@link FileUploadHandler} instance to handle uploaded files
     */
    public void setHandler(FileUploadHandler handler) {
        if (this.handler != null && !this.handler.equals(handler)) {
            this.queueForDispatch(DOMUpdateType.REMOVE_EVENT_LISTENER, DOMUpdateParam.EVENT_NAME, "change");
        } else {
            this.queueForDispatch(DOMUpdateType.ADD_EVENT_LISTENER, DOMUpdateParam.EVENT_NAME, "change");
        }
        this.handler = handler;
    }

    /**
     * Gets the currently assigned file upload handler.
     *
     * @return the handler or null if none assigned
     */
    public FileUploadHandler getHandler() {
        return handler;
    }

    /**
     * Callback interface for processing uploaded files.
     */
    public interface FileUploadHandler {
        /**
         * Called when a file is uploaded.
         *
         * @param fileName the name of the uploaded file
         * @param data     the raw byte content of the uploaded file
         */
        void handle(String fileName, byte[] data);
    }
}