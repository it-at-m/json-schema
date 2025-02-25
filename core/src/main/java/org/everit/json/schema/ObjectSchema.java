package org.everit.json.schema;

import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.Regexp;
import org.everit.json.schema.regexp.RegexpFactory;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.everit.json.schema.JSONPointer.unescape;

/**
 * Object schema validator.
 */
public class ObjectSchema extends Schema {

    /**
     * Builder class for {@link ObjectSchema}.
     */
    public static class Builder extends Schema.Builder<ObjectSchema> {

        private static final RegexpFactory DEFAULT_REGEXP_FACTORY = new JavaUtilRegexpFactory();

        private static final Regexp toRegexp(String pattern) {
            return DEFAULT_REGEXP_FACTORY.createHandler(pattern);
        }

        private final Map<Regexp, Schema> patternProperties = new HashMap<>();

        private boolean requiresObject = true;

        private final Map<String, Schema> propertySchemas = new HashMap<>();

        private boolean additionalProperties = true;

        private Schema schemaOfAdditionalProperties;

        private final List<String> requiredProperties = new ArrayList<String>(0);

        private Integer minProperties;

        private Integer maxProperties;

        private final Map<String, Set<String>> propertyDependencies = new HashMap<>();

        private final Map<String, Schema> schemaDependencies = new HashMap<>();

        private Schema propertyNameSchema;

        public boolean oneOrMoreDefaultProperty = false;

        public Builder additionalProperties(boolean additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        /**
         * Adds a property schema.
         *
         * @param propName the name of the property which' expected schema must be {@code schema}
         * @param schema   if the subject under validation has a property named {@code propertyName} then its
         *                 value will be validated using this {@code schema}
         * @return {@code this}
         */
        public Builder addPropertySchema(String propName, Schema schema) {
            requireNonNull(propName, "propName cannot be null");
            requireNonNull(schema, "schema cannot be null");
            propertySchemas.put(propName, schema);
            oneOrMoreDefaultProperty |= schema.hasDefaultValue();
            return this;
        }

        public Builder addRequiredProperty(String propertyName) {
            requiredProperties.add(propertyName);
            return this;
        }

        @Override
        public ObjectSchema build() {
            return new ObjectSchema(this);
        }

        public Builder maxProperties(Integer maxProperties) {
            this.maxProperties = maxProperties;
            return this;
        }

        public Builder minProperties(Integer minProperties) {
            this.minProperties = minProperties;
            return this;
        }

        @Deprecated
        public Builder patternProperty(java.util.regex.Pattern pattern, Schema schema) {
            Regexp handler = toRegexp(pattern.toString());
            return patternProperty(handler, schema);
        }

        @Deprecated
        public Builder patternProperty(String pattern, Schema schema) {
            Regexp handler = toRegexp(pattern);
            return patternProperty(handler, schema);
        }

        public Builder patternProperty(Regexp pattern, Schema schema) {
            this.patternProperties.put(pattern, schema);
            return this;
        }

        /**
         * Adds a property dependency.
         *
         * @param ifPresent     the name of the property which if is present then a property with name
         *                      {@code mustBePresent} is mandatory
         * @param mustBePresent a property with this name must exist in the subject under validation if a property
         *                      named {@code ifPresent} exists
         * @return {@code this}
         */
        public Builder propertyDependency(String ifPresent, String mustBePresent) {
            Set<String> dependencies = propertyDependencies.get(ifPresent);
            if (dependencies == null) {
                dependencies = new HashSet<>(1);
                propertyDependencies.put(ifPresent, dependencies);
            }
            dependencies.add(mustBePresent);
            return this;
        }

        public Builder requiresObject(boolean requiresObject) {
            this.requiresObject = requiresObject;
            return this;
        }

        public Builder schemaDependency(String ifPresent, Schema expectedSchema) {
            schemaDependencies.put(ifPresent, expectedSchema);
            return this;
        }

        public Builder schemaOfAdditionalProperties(Schema schemaOfAdditionalProperties) {
            this.schemaOfAdditionalProperties = schemaOfAdditionalProperties;
            return this;
        }

        public Builder propertyNameSchema(Schema propertyNameSchema) {
            this.propertyNameSchema = propertyNameSchema;
            return this;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private static <K, V> Map<K, V> copyMap(Map<K, V> original) {
        return Collections.unmodifiableMap(new HashMap<>(original));
    }

    private final Map<String, Schema> propertySchemas;

    private final boolean additionalProperties;

    private final Schema schemaOfAdditionalProperties;

    private final Schema propertyNameSchema;

    private final List<String> requiredProperties;

    private final Integer minProperties;

    private final Integer maxProperties;

    private final Map<String, Set<String>> propertyDependencies;

    private final Map<String, Schema> schemaDependencies;

    private final boolean requiresObject;

    private final Map<Regexp, Schema> patternProperties;

    private final boolean oneOrMoreDefaultProperty;

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public ObjectSchema(Builder builder) {
        super(builder);
        this.propertySchemas = builder.propertySchemas == null ? null
                : Collections.unmodifiableMap(builder.propertySchemas);
        this.additionalProperties = builder.additionalProperties;
        this.schemaOfAdditionalProperties = builder.schemaOfAdditionalProperties;
        if (!additionalProperties && schemaOfAdditionalProperties != null) {
            throw new SchemaException(
                    "additionalProperties cannot be false if schemaOfAdditionalProperties is present");
        }
        this.requiredProperties = Collections.unmodifiableList(new ArrayList<>(
                builder.requiredProperties));
        this.minProperties = builder.minProperties;
        this.maxProperties = builder.maxProperties;
        this.propertyDependencies = copyMap(builder.propertyDependencies);
        this.schemaDependencies = copyMap(builder.schemaDependencies);
        this.requiresObject = builder.requiresObject;
        this.patternProperties = copyMap(builder.patternProperties);
        this.propertyNameSchema = builder.propertyNameSchema;
        this.oneOrMoreDefaultProperty = builder.oneOrMoreDefaultProperty;
    }

    public Integer getMaxProperties() {
        return maxProperties;
    }

    public Integer getMinProperties() {
        return minProperties;
    }

    Map<Regexp, Schema> getRegexpPatternProperties() {
        return patternProperties;
    }

    @Deprecated
    public Map<Pattern, Schema> getPatternProperties() {
        return patternProperties.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(java.util.regex.Pattern.compile(entry.getKey().toString()), entry.getValue()))
                .collect(toMap(
                        (Map.Entry<java.util.regex.Pattern, Schema> entry) -> entry.getKey(),
                        (Map.Entry<java.util.regex.Pattern, Schema> entry) -> entry.getValue()
                ));
    }

    public Map<String, Set<String>> getPropertyDependencies() {
        return propertyDependencies;
    }

    public Map<String, Schema> getPropertySchemas() {
        return propertySchemas;
    }

    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    public Map<String, Schema> getSchemaDependencies() {
        return schemaDependencies;
    }

    public Schema getSchemaOfAdditionalProperties() {
        return schemaOfAdditionalProperties;
    }

    public Schema getPropertyNameSchema() {
        return propertyNameSchema;
    }

    @Override
    void accept(Visitor visitor) {
        visitor.visitObjectSchema(this);
    }

    public boolean permitsAdditionalProperties() {
        return additionalProperties;
    }

    public boolean requiresObject() {
        return requiresObject;
    }

    boolean hasDefaultProperty() {
        return oneOrMoreDefaultProperty;
    }

    @Override
    public boolean isReadOnlyProperty(String field) {
        if (Boolean.TRUE.equals(this.isReadOnly())) {
            return this.definesProperty(field);
        }

        String[] headAndTail = headAndTailOfJsonPointerFragment(field);
        String nextToken = headAndTail[0];
        String remaining = headAndTail[1];
        field = headAndTail[2];
        return !field.isEmpty() && (definesIsReadOnlySchemaProperty(nextToken, remaining));
    }

    private boolean definesIsReadOnlySchemaProperty(String current, String remaining) {
        current = unescape(current);
        boolean hasSuffix = !(remaining == null);
        if (propertySchemas.containsKey(current)) {
            if (hasSuffix) {
                return propertySchemas.get(current).isReadOnlyProperty(remaining);
            } else {
                return Boolean.TRUE.equals(propertySchemas.get(current).isReadOnly());
            }
        }
        return false;
    }

    @Override
    public boolean definesProperty(String field) {
        String[] headAndTail = headAndTailOfJsonPointerFragment(field);
        String nextToken = headAndTail[0];
        String remaining = headAndTail[1];
        field = headAndTail[2];
        return !field.isEmpty() && (definesSchemaProperty(nextToken, remaining)
                || definesPatternProperty(nextToken, remaining)
                || definesSchemaDependencyProperty(field));
    }

    private boolean definesSchemaProperty(String current, String remaining) {
        current = unescape(current);
        boolean hasSuffix = !(remaining == null);
        if (propertySchemas.containsKey(current)) {
            if (hasSuffix) {
                return propertySchemas.get(current).definesProperty(remaining);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean definesPatternProperty(String current, String remaining) {
        for (Map.Entry<Regexp, Schema> entry : patternProperties.entrySet()) {
            Regexp pattern = entry.getKey();
            if (!pattern.patternMatchingFailure(current).isPresent()) {
                if (remaining == null || entry.getValue().definesProperty(remaining)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean definesSchemaDependencyProperty(String field) {
        if (schemaDependencies.containsKey(field)) {
            return true;
        }
        for (Schema schema : schemaDependencies.values()) {
            if (schema.definesProperty(field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ObjectSchema) {
            ObjectSchema that = (ObjectSchema) o;
            return that.canEqual(this) &&
                    additionalProperties == that.additionalProperties &&
                    requiresObject == that.requiresObject &&
                    Objects.equals(propertySchemas, that.propertySchemas) &&
                    Objects.equals(schemaOfAdditionalProperties, that.schemaOfAdditionalProperties) &&
                    Objects.equals(requiredProperties, that.requiredProperties) &&
                    Objects.equals(minProperties, that.minProperties) &&
                    Objects.equals(maxProperties, that.maxProperties) &&
                    Objects.equals(propertyDependencies, that.propertyDependencies) &&
                    Objects.equals(schemaDependencies, that.schemaDependencies) &&
                    Objects.equals(patternProperties, that.patternProperties) &&
                    Objects.equals(propertyNameSchema, that.propertyNameSchema) &&
                    oneOrMoreDefaultProperty == that.oneOrMoreDefaultProperty &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertySchemas, propertyNameSchema, additionalProperties,
                schemaOfAdditionalProperties, requiredProperties, minProperties, maxProperties, propertyDependencies,
                schemaDependencies, requiresObject, patternProperties, oneOrMoreDefaultProperty);
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ObjectSchema;
    }
}
