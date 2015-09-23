/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jsonvalidator.loader;

import java.io.InputStream;
import java.util.Map;

import org.everit.jsonvalidator.ArraySchema;
import org.everit.jsonvalidator.BooleanSchema;
import org.everit.jsonvalidator.CombinedSchema;
import org.everit.jsonvalidator.EmptySchema;
import org.everit.jsonvalidator.EnumSchema;
import org.everit.jsonvalidator.NotSchema;
import org.everit.jsonvalidator.NullSchema;
import org.everit.jsonvalidator.NumberSchema;
import org.everit.jsonvalidator.ObjectSchema;
import org.everit.jsonvalidator.Schema;
import org.everit.jsonvalidator.SchemaException;
import org.everit.jsonvalidator.StringSchema;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaLoaderTest {

  private static JSONObject ALL_SCHEMAS;

  @BeforeClass
  public static void before() {
    InputStream stream = SchemaLoaderTest.class.getResourceAsStream(
        "/org/everit/jsonvalidator/testschemas.json");
    ALL_SCHEMAS = new JSONObject(new JSONTokener(stream));
  }

  @Test
  public void additionalItemSchema() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("additionalItemSchema"));
  }

  @Test
  public void arrayByAdditionalItems() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByAdditionalItems"));
    Assert.assertFalse(actual.requiresArray());
  }

  @Test
  public void arrayByItems() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arrayByItems"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void arraySchema() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("arraySchema"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(2, actual.getMinItems().intValue());
    Assert.assertEquals(3, actual.getMaxItems().intValue());
    Assert.assertTrue(actual.needsUniqueItems());
    Assert.assertEquals(NullSchema.INSTANCE, actual.getAllItemSchema());
  }

  @Test
  public void booleanSchema() {
    BooleanSchema actual = (BooleanSchema) SchemaLoader.load(get("booleanSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void combinedSchemaLoading() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("combinedSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void emptySchema() {
    EmptySchema actual = (EmptySchema) SchemaLoader.load(get("emptySchema"));
  }

  private JSONObject get(final String schemaName) {
    return ALL_SCHEMAS.getJSONObject(schemaName);
  }

  @Test
  public void integerSchema() {
    NumberSchema actual = (NumberSchema) SchemaLoader.load(get("integerSchema"));
    Assert.assertEquals(10, actual.getMinimum().intValue());
    Assert.assertEquals(20, actual.getMaximum().intValue());
    Assert.assertEquals(5, actual.getMultipleOf().intValue());
    Assert.assertTrue(actual.isExclusiveMinimum());
    Assert.assertTrue(actual.isExclusiveMaximum());
    Assert.assertTrue(actual.requiresInteger());
  }

  @Test(expected = SchemaException.class)
  public void invalidAdditionalItems() {
    SchemaLoader.load(get("invalidAdditionalItems"));
  }

  @Test(expected = SchemaException.class)
  public void invalidArrayItemSchema() {
    SchemaLoader.load(get("invalidArrayItemSchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidDependency() {
    SchemaLoader.load(get("invalidDependency"));
  }

  @Test(expected = SchemaException.class)
  public void invalidExclusiveMinimum() {
    SchemaLoader.load(get("invalidExclusiveMinimum"));
  }

  @Test(expected = SchemaException.class)
  public void invalidIntegerSchema() {
    JSONObject input = get("invalidIntegerSchema");
    SchemaLoader.load(input);
  }

  @Test(expected = SchemaException.class)
  public void invalidItemsArraySchema() {
    SchemaLoader.load(get("invalidItemsArraySchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidStringSchema() {
    SchemaLoader.load(get("invalidStringSchema"));
  }

  @Test(expected = SchemaException.class)
  public void invalidType() {
    SchemaLoader.load(get("invalidType"));
  }

  @Test
  public void multipleTypes() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("multipleTypes"));
  }

  @Test
  public void neverMatchingAnyOf() {
    CombinedSchema actual = (CombinedSchema) SchemaLoader.load(get("anyOfNeverMatches"));
  }

  @Test
  public void noExplicitObject() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("noExplicitObject"));
    Assert.assertFalse(actual.requiresObject());
  }

  @Test
  public void notSchema() {
    NotSchema actual = (NotSchema) SchemaLoader.load(get("notSchema"));
    Assert.assertNotNull(actual);
  }

  @Test
  public void nullSchema() {
    NullSchema actual = (NullSchema) SchemaLoader.load(get("nullSchema"));
    Assert.assertNotNull(actual);
  }

  @Test(expected = SchemaException.class)
  public void objectInvalidAdditionalProperties() {
    SchemaLoader.load(get("objectInvalidAdditionalProperties"));
  }

  @Test
  public void objectSchema() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectSchema"));
    Assert.assertNotNull(actual);
    Map<String, Schema> propertySchemas = actual.getPropertySchemas();
    Assert.assertEquals(2, propertySchemas.size());
    Assert.assertEquals(BooleanSchema.INSTANCE, propertySchemas.get("boolProp"));
    Assert.assertFalse(actual.permitsAdditionalProperties());
    Assert.assertEquals(2, actual.getRequiredProperties().size());
    Assert.assertEquals(2, actual.getMinProperties().intValue());
    Assert.assertEquals(3, actual.getMaxProperties().intValue());
  }

  @Test
  public void objectWithAdditionalPropSchema() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithAdditionalPropSchema"));
    Assert.assertEquals(BooleanSchema.INSTANCE, actual.getSchemaOfAdditionalProperties());
  }

  @Test
  public void objectWithPropDep() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithPropDep"));
    Assert.assertEquals(1, actual.getPropertyDependencies().get("isIndividual").size());
  }

  @Test
  public void objectWithSchemaDep() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("objectWithSchemaDep"));
    Assert.assertEquals(1, actual.getSchemaDependencies().size());
  }

  @Test
  public void pointerResolution() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("pointerResolution"));
    ObjectSchema rectangleSchema = (ObjectSchema) actual.getPropertySchemas().get("rectangle");
    Assert.assertNotNull(rectangleSchema);
    Assert.assertTrue(rectangleSchema.getPropertySchemas().get("a") instanceof NumberSchema);
  }

  @Test(expected = SchemaException.class)
  public void pointerResolutionFailure() {
    SchemaLoader.load(get("pointerResolutionFailure"));
  }

  @Test
  public void stringSchema() {
    StringSchema actual = (StringSchema) SchemaLoader.load(get("stringSchema"));
    Assert.assertEquals(2, actual.getMinLength().intValue());
    Assert.assertEquals(3, actual.getMaxLength().intValue());
  }

  @Test
  public void tupleSchema() {
    ArraySchema actual = (ArraySchema) SchemaLoader.load(get("tupleSchema"));
    Assert.assertFalse(actual.permitsAdditionalItems());
    Assert.assertNull(actual.getAllItemSchema());
    Assert.assertEquals(2, actual.getItemSchemas().size());
    Assert.assertEquals(BooleanSchema.INSTANCE, actual.getItemSchemas().get(0));
    Assert.assertEquals(NullSchema.INSTANCE, actual.getItemSchemas().get(1));
  }

  @Test(expected = SchemaException.class)
  public void unknownSchema() {
    SchemaLoader.load(get("unknown"));
  }

  @Test
  public void enumSchema() {
    EnumSchema actual = (EnumSchema) SchemaLoader.load(get("enumSchema"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(4, actual.getPossibleValues().size());
  }

  @Test
  public void patternProperties() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("patternProperties"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(2, actual.getPatternProperties().size());
  }

  @Test
  public void emptyPatternProperties() {
    ObjectSchema actual = (ObjectSchema) SchemaLoader.load(get("emptyPatternProperties"));
    Assert.assertNotNull(actual);
    Assert.assertEquals(0, actual.getPatternProperties().size());
  }
}