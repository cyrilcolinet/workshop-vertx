package eu.epitech.dashboard.api.widgets;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class of widget
 * All widgets must be extended with this class
 * All widgets depends to a {@see AbstractService} class
 */
public abstract class AbstractWidget {

    private String name;
    private String description;
    private List<Parameter> params = new ArrayList<>();

    /**
     * AbstractWidget constructor.
     * @param name Name of the widget
     * @param description Small description of the widget
     */
    public AbstractWidget(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Get name of the widget
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get small description of the widget
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Add parameter to widget
     * Parameter must be added in constructor of widget
     * @param param {@see Parameter}
     */
    protected void addParam(Parameter param) {
        this.params.add(param);
    }

    /**
     * Get all configured parameters
     * @return List of parameters
     */
    public List<Parameter> getParams() {
        return params;
    }

    /**
     * Parameter class for widget
     */
    public static class Parameter {

        private String name;
        private String type;
        private String description;

        /**
         * Local class Parameter constructor
         * @param name Name of the parameter
         * @param type Type of the parameter (must be "string" or "integer")
         * @param description Description of the content
         */
        public Parameter(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

        /**
         * Local class Parameter constructor
         * @param name Name of the parameter
         * @param type Type of the parameter (must be "string" or "integer")
         */
        public Parameter(String name, String type) {
            this.name = name;
            this.type = type;
            this.description = name;
        }

        /**
         * Get name of parameter
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Get type of parameter
         * @return String ("string" or "integer")
         */
        public String getType() {
            return type;
        }

        /**
         * Get description of field
         * @return String Description of the parameter
         */
        public String getDescription() {
            return description;
        }
    }
}
