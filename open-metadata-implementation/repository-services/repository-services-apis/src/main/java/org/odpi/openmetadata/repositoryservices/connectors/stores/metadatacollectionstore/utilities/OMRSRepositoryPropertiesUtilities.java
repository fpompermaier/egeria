/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.utilities;

import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.*;
import org.odpi.openmetadata.repositoryservices.ffdc.OMRSErrorCode;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * OMRSRepositoryPropertiesUtilities implements the methods to add and remove values from InstanceProperties
 * objects as defined by the OMRSRepositoryPropertiesHelper interface.
 */
public class OMRSRepositoryPropertiesUtilities implements OMRSRepositoryPropertiesHelper
{
    private static final Logger log = LoggerFactory.getLogger(OMRSRepositoryPropertiesUtilities.class);

    /**
     * Remove the named property from the instance properties object.
     *
     * @param propertyName name of property to remove
     * @param properties instance properties object to work on
     */
    protected void removeProperty(String    propertyName, InstanceProperties properties)
    {
        if (properties != null)
        {
            Map<String, InstancePropertyValue> instancePropertyValueMap = properties.getInstanceProperties();

            if (instancePropertyValueMap != null)
            {
                instancePropertyValueMap.remove(propertyName);
                properties.setInstanceProperties(instancePropertyValueMap);
            }
        }
    }



    /**
     * Return the requested property or null if property is not found.  If the property is not
     * a string property then a logic exception is thrown
     *
     * @param sourceName source of call
     * @param propertyName name of requested property
     * @param properties properties from the instance.
     * @param methodName method of caller
     * @return string property value or null
     */
    public String getStringProperty(String             sourceName,
                                    String             propertyName,
                                    InstanceProperties properties,
                                    String             methodName)
    {
        final String  thisMethodName = "getStringProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                    {
                        PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) instancePropertyValue;

                        if (primitivePropertyValue.getPrimitiveDefCategory() == PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING)
                        {
                            if (primitivePropertyValue.getPrimitiveValue() != null)
                            {
                                String retrievedProperty = primitivePropertyValue.getPrimitiveValue().toString();
                                log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);

                                return retrievedProperty;
                            }
                        }
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("No " + propertyName + " property");
        return null;
    }


    /**
     * Return the requested property or null if property is not found.  If the property is found, it is removed from
     * the InstanceProperties structure.  If the property is not a string property then a logic exception is thrown.
     *
     * @param sourceName  source of call
     * @param propertyName  name of requested property
     * @param properties  properties from the instance.
     * @param methodName  method of caller
     * @return string property value or null
     */
    public String removeStringProperty(String             sourceName,
                                       String             propertyName,
                                       InstanceProperties properties,
                                       String             methodName)
    {
        String  retrievedProperty = null;

        if (properties != null)
        {
            retrievedProperty = this.getStringProperty(sourceName, propertyName, properties, methodName);

            if (retrievedProperty != null)
            {
                this.removeProperty(propertyName, properties);
                log.debug("Properties left: " + properties.toString());
            }
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }


    /**
     * Return the requested property or null if property is not found.  If the property is not
     * a map property then a logic exception is thrown
     *
     * @param sourceName source of call
     * @param propertyName name of requested property
     * @param properties properties from the instance.
     * @param methodName method of caller
     * @return string property value or null
     */
    public InstanceProperties getMapProperty(String             sourceName,
                                             String             propertyName,
                                             InstanceProperties properties,
                                             String             methodName)
    {
        final String  thisMethodName = "getMapProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.MAP)
                    {
                        MapPropertyValue mapPropertyValue = (MapPropertyValue) instancePropertyValue;

                        log.debug("Retrieved map property " + propertyName);

                        return mapPropertyValue.getMapValues();
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("Map property " + propertyName + " not present");
        return null;
    }


    /**
     * Locates and extracts a string array property and extracts its values.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties all of the properties of the instance
     * @param callingMethodName method of caller
     * @return array property value or null
     */
    public List<String> getStringArrayProperty(String             sourceName,
                                               String             propertyName,
                                               InstanceProperties properties,
                                               String             callingMethodName)
    {
        final String  thisMethodName = "getStringArrayProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                /*
                 * The property exists in the supplied properties.   It should be of category ARRAY.
                 * If it is then it can be cast to an ArrayPropertyValue in order to extract the
                 * array size and the values.
                 */
                log.debug(thisMethodName + "retrieved array property " + propertyName + " for " + callingMethodName);

                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.ARRAY)
                    {
                        ArrayPropertyValue arrayPropertyValue = (ArrayPropertyValue) instancePropertyValue;

                        if ((arrayPropertyValue != null) && (arrayPropertyValue.getArrayCount() > 0))
                        {
                            /*
                             * There are values to extract
                             */
                            log.debug(thisMethodName + " found that array property " + propertyName + " has " + arrayPropertyValue.getArrayCount() + " elements.");

                            return getInstancePropertiesAsArray(arrayPropertyValue.getArrayValues(), callingMethodName);
                        }
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, callingMethodName, thisMethodName);
                }
            }
        }

        log.debug(propertyName + " not present in " + properties);
        return null;
    }


    /**
     * Locates and extracts a string array property and extracts its values.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not an array property then a logic exception is thrown.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties all of the properties of the instance
     * @param methodName method of caller
     * @return array property value or null
     */
    public List<String> removeStringArrayProperty(String             sourceName,
                                                  String             propertyName,
                                                  InstanceProperties properties,
                                                  String             methodName)
    {
        List<String>  retrievedProperty = null;

        if (properties != null)
        {
            retrievedProperty = this.getStringArrayProperty(sourceName, propertyName, properties, methodName);

            if (retrievedProperty != null)
            {
                this.removeProperty(propertyName, properties);
                log.debug("Properties left: " + properties.toString());
            }
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }


    /**
     * Convert the values in the instance properties into a String Array.  It assumes all of the elements are primitives.
     *
     * @param instanceProperties instance properties containing the values.  They should all be primitive Strings.
     * @param callingMethodName method of caller
     * @return list of strings
     */
    public List<String> getInstancePropertiesAsArray(InstanceProperties     instanceProperties,
                                                     String                 callingMethodName)
    {
        final String  thisMethodName = "getInstancePropertiesAsArray";

        if (instanceProperties != null)
        {
            Map<String, InstancePropertyValue> instancePropertyValues = instanceProperties.getInstanceProperties();
            List<String>                       resultingArray = new ArrayList<>();

            for (String arrayOrdinalName : instancePropertyValues.keySet())
            {
                if (arrayOrdinalName != null)
                {
                    log.debug(thisMethodName + " processing array element: " + arrayOrdinalName);

                    int                   arrayOrdinalNumber  = Integer.decode(arrayOrdinalName);
                    InstancePropertyValue actualPropertyValue = instanceProperties.getPropertyValue(arrayOrdinalName);

                    if (actualPropertyValue != null)
                    {
                        if (actualPropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                        {
                            PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) actualPropertyValue;
                            resultingArray.add(arrayOrdinalNumber, primitivePropertyValue.getPrimitiveValue().toString());
                        }
                        else
                        {
                            log.error(thisMethodName + " skipping collection value: " + actualPropertyValue + " from method " + callingMethodName);
                        }
                    }
                    else
                    {
                        log.error(thisMethodName + " skipping null value" + " from method " + callingMethodName);
                    }
                }
                else
                {
                    log.error(thisMethodName + " skipping null ordinal" + " from method " + callingMethodName);
                }
            }

            log.debug(thisMethodName + " returning array: " + resultingArray + " to method " + callingMethodName);
            return resultingArray;
        }

        log.debug(thisMethodName + " has no property values to extract for method " + callingMethodName);
        return null;
    }


    /**
     * Locates and extracts a property from an instance that is of type map and then converts its values into a Java map.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties values of the property
     * @param methodName method of caller
     * @return map property value or null
     */
    public Map<String, String> getStringMapFromProperty(String             sourceName,
                                                        String             propertyName,
                                                        InstanceProperties properties,
                                                        String             methodName)
    {
        Map<String, Object>   mapFromProperty = this.getMapFromProperty(sourceName, propertyName, properties, methodName);

        if (mapFromProperty != null)
        {
            Map<String, String>  stringMapFromProperty = new HashMap<>();

            for (String mapPropertyName : mapFromProperty.keySet())
            {
                Object actualPropertyValue = mapFromProperty.get(mapPropertyName);

                if (actualPropertyValue != null)
                {
                    stringMapFromProperty.put(mapPropertyName, actualPropertyValue.toString());
                }
            }

            if (! stringMapFromProperty.isEmpty())
            {
                return stringMapFromProperty;
            }
        }

        return null;
    }


    /**
     * Locates and extracts a property from an instance that is of type map and then converts its values into a Java map.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not a map property then a logic exception is thrown.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties values of the property
     * @param methodName method of caller
     * @return map property value or null
     */
    public Map<String, String> removeStringMapFromProperty(String             sourceName,
                                                           String             propertyName,
                                                           InstanceProperties properties,
                                                           String             methodName)
    {
        Map<String, String>  retrievedProperty = null;

        if (properties != null)
        {
            retrievedProperty = this.getStringMapFromProperty(sourceName, propertyName, properties, methodName);

            if (retrievedProperty != null)
            {
                this.removeProperty(propertyName, properties);
                log.debug("Properties left: " + properties.toString());
            }
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }


    /**
     * Locates and extracts a property from an instance that is of type map and then converts its values into a Java map.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties all of the properties of the instance
     * @param methodName method of caller
     * @return map property value or null
     */
    public Map<String, Object> getMapFromProperty(String             sourceName,
                                                  String             propertyName,
                                                  InstanceProperties properties,
                                                  String             methodName)
    {
        final String  thisMethodName = "getMapFromProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.MAP)
                    {
                        MapPropertyValue mapInstancePropertyValue = (MapPropertyValue) instancePropertyValue;

                        log.debug("Retrieved map property " + propertyName);

                        return this.getInstancePropertiesAsMap(mapInstancePropertyValue.getMapValues());
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("Map property " + propertyName + " not present");
        return null;
    }


    /**
     * Locates and extracts a property from an instance that is of type map and then converts its values into a Java map.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not a map property then a logic exception is thrown.
     *
     * @param sourceName source of call
     * @param propertyName name of requested map property
     * @param properties values of the property
     * @param methodName method of caller
     * @return map property value or null
     */
    public Map<String, Object> removeMapFromProperty(String             sourceName,
                                                     String             propertyName,
                                                     InstanceProperties properties,
                                                     String             methodName)
    {
        Map<String, Object>  retrievedProperty = null;

        if (properties != null)
        {
            retrievedProperty = this.getMapFromProperty(sourceName, propertyName, properties, methodName);

            if (retrievedProperty != null)
            {
                this.removeProperty(propertyName, properties);
                log.debug("Properties left: " + properties.toString());
            }
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }



    /**
     * Convert an instance properties object into a map.
     *
     * @param instanceProperties packed properties
     * @return properties stored in Java map
     */
    public Map<String, Object> getInstancePropertiesAsMap(InstanceProperties    instanceProperties)
    {
        if (instanceProperties != null)
        {
            Map<String, InstancePropertyValue> instancePropertyValues = instanceProperties.getInstanceProperties();
            Map<String, Object>                resultingMap      = new HashMap<>();

            if (instancePropertyValues != null)
            {
                for (String mapPropertyName : instancePropertyValues.keySet())
                {
                    InstancePropertyValue actualPropertyValue = instanceProperties.getPropertyValue(mapPropertyName);

                    if (actualPropertyValue != null)
                    {
                        if (actualPropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                        {
                            PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) actualPropertyValue;
                            resultingMap.put(mapPropertyName, primitivePropertyValue.getPrimitiveValue());
                        }
                        else if (actualPropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.ENUM)
                        {
                            EnumPropertyValue  enumPropertyValue = (EnumPropertyValue) actualPropertyValue;
                            resultingMap.put(mapPropertyName, enumPropertyValue.getSymbolicName());
                        }
                        else
                        {
                            resultingMap.put(mapPropertyName, actualPropertyValue);
                        }
                    }
                }
            }

            log.debug("Returning map: " + resultingMap);
            return resultingMap;
        }

        log.debug("No Properties present");
        return null;
    }



    /**
     * Return the requested property or 0 if property is not found.  If the property is not
     * an int property then a logic exception is thrown.
     *
     * @param sourceName source of call
     * @param propertyName name of requested property
     * @param properties properties from the instance.
     * @param methodName method of caller
     * @return string property value or null
     */
    public int    getIntProperty(String             sourceName,
                                 String             propertyName,
                                 InstanceProperties properties,
                                 String             methodName)
    {
        final String  thisMethodName = "getIntProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                    {
                        PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) instancePropertyValue;

                        if (primitivePropertyValue.getPrimitiveDefCategory() == PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT)
                        {
                            log.debug("Retrieved integer property " + propertyName);

                            if (primitivePropertyValue.getPrimitiveValue() != null)
                            {
                                return Integer.valueOf(primitivePropertyValue.getPrimitiveValue().toString());
                            }
                        }
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("Integer property " + propertyName + " not present");

        return 0;
    }


    /**
     * Return the requested property or 0 if property is not found.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not an int property then a logic exception is thrown.
     *
     * @param sourceName  source of call
     * @param propertyName  name of requested property
     * @param properties  properties from the instance.
     * @param methodName  method of caller
     * @return string property value or null
     */
    public int    removeIntProperty(String             sourceName,
                                    String             propertyName,
                                    InstanceProperties properties,
                                    String             methodName)
    {
        int  retrievedProperty = 0;

        if (properties != null)
        {
            retrievedProperty = this.getIntProperty(sourceName, propertyName, properties, methodName);

            this.removeProperty(propertyName, properties);
            log.debug("Properties left: " + properties.toString());
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }


    /**
     * Return the requested property or null if property is not found.  If the property is not
     * a date property then a logic exception is thrown.
     *
     * @param sourceName source of call
     * @param propertyName name of requested property
     * @param properties properties from the instance.
     * @param methodName method of caller
     * @return string property value or null
     */
    public Date getDateProperty(String             sourceName,
                                String             propertyName,
                                InstanceProperties properties,
                                String             methodName)
    {
        final String  thisMethodName = "getDateProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                    {
                        PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) instancePropertyValue;

                        if (primitivePropertyValue.getPrimitiveDefCategory() == PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE)
                        {
                            log.debug("Retrieved date property " + propertyName);

                            if (primitivePropertyValue.getPrimitiveValue() != null)
                            {
                                Long timestamp = (Long)primitivePropertyValue.getPrimitiveValue();
                                return new Date(timestamp);

                            }
                        }
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("Date property " + propertyName + " not present");

        return null;
    }


    /**
     * Return the requested property or null if property is not found.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not a date property then a logic exception is thrown.
     *
     * @param sourceName  source of call
     * @param propertyName  name of requested property
     * @param properties  properties from the instance.
     * @param methodName  method of caller
     * @return string property value or null
     */
    public Date    removeDateProperty(String             sourceName,
                                      String             propertyName,
                                      InstanceProperties properties,
                                      String             methodName)
    {
        Date  retrievedProperty = null;

        if (properties != null)
        {
            retrievedProperty = this.getDateProperty(sourceName, propertyName, properties, methodName);

            this.removeProperty(propertyName, properties);
            log.debug("Properties left: " + properties.toString());
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }



    /**
     * Return the requested property or false if property is not found.  If the property is not
     * a boolean property then a logic exception is thrown
     *
     * @param sourceName source of call
     * @param propertyName name of requested property
     * @param properties properties from the instance.
     * @param methodName method of caller
     * @return string property value or null
     */
    public boolean getBooleanProperty(String             sourceName,
                                      String             propertyName,
                                      InstanceProperties properties,
                                      String             methodName)
    {
        final String  thisMethodName = "getBooleanProperty";

        if (properties != null)
        {
            InstancePropertyValue instancePropertyValue = properties.getPropertyValue(propertyName);

            if (instancePropertyValue != null)
            {
                try
                {
                    if (instancePropertyValue.getInstancePropertyCategory() == InstancePropertyCategory.PRIMITIVE)
                    {
                        PrimitivePropertyValue primitivePropertyValue = (PrimitivePropertyValue) instancePropertyValue;

                        if (primitivePropertyValue.getPrimitiveDefCategory() == PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN)
                        {
                            log.debug("Retrieved boolean property " + propertyName);

                            if (primitivePropertyValue.getPrimitiveValue() != null)
                            {
                                return Boolean.valueOf(primitivePropertyValue.getPrimitiveValue().toString());
                            }
                        }
                    }
                }
                catch (Throwable error)
                {
                    throwHelperLogicError(sourceName, methodName, thisMethodName);
                }
            }
        }

        log.debug("Boolean property " + propertyName + " not present");

        return false;
    }


    /**
     * Return the requested property or false if property is not found.
     * If the property is found, it is removed from the InstanceProperties structure.
     * If the property is not a boolean property then a logic exception is thrown.
     *
     * @param sourceName  source of call
     * @param propertyName  name of requested property
     * @param properties  properties from the instance.
     * @param methodName  method of caller
     * @return string property value or null
     */
    public boolean removeBooleanProperty(String             sourceName,
                                         String             propertyName,
                                         InstanceProperties properties,
                                         String             methodName)
    {
        boolean  retrievedProperty = false;

        if (properties != null)
        {
            retrievedProperty = this.getBooleanProperty(sourceName, propertyName, properties, methodName);

            this.removeProperty(propertyName, properties);
            log.debug("Properties left: " + properties.toString());
        }

        log.debug("Retrieved " + propertyName + " property: " + retrievedProperty);
        return retrievedProperty;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param propertyValue value of property
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addStringPropertyToInstance(String             sourceName,
                                                          InstanceProperties properties,
                                                          String             propertyName,
                                                          String             propertyValue,
                                                          String             methodName)
    {
        InstanceProperties  resultingProperties;

        if (propertyValue != null)
        {
            log.debug("Adding property " + propertyName + " for " + methodName);

            if (properties == null)
            {
                log.debug("First property");

                resultingProperties = new InstanceProperties();
            }
            else
            {
                resultingProperties = properties;
            }


            PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

            primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING);
            primitivePropertyValue.setPrimitiveValue(propertyValue);
            primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getName());
            primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getGUID());

            resultingProperties.setProperty(propertyName, primitivePropertyValue);

            return resultingProperties;
        }
        else
        {
            log.debug("Null property");
            return properties;
        }
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param propertyValue value of property
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addIntPropertyToInstance(String             sourceName,
                                                       InstanceProperties properties,
                                                       String             propertyName,
                                                       int                propertyValue,
                                                       String             methodName)
    {
        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

        primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT);
        primitivePropertyValue.setPrimitiveValue(propertyValue);
        primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT.getName());
        primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT.getGUID());

        resultingProperties.setProperty(propertyName, primitivePropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName  name of caller
     * @param properties  properties object to add property to may be null.
     * @param propertyName  name of property
     * @param propertyValue  value of property
     * @param methodName  calling method name
     * @return instance properties object.
     */
    public InstanceProperties addLongPropertyToInstance(String             sourceName,
                                                        InstanceProperties properties,
                                                        String             propertyName,
                                                        long               propertyValue,
                                                        String             methodName)
    {
        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

        primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_LONG);
        primitivePropertyValue.setPrimitiveValue(propertyValue);

        resultingProperties.setProperty(propertyName, primitivePropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName  name of caller
     * @param properties  properties object to add property to may be null.
     * @param propertyName  name of property
     * @param propertyValue  value of property
     * @param methodName  calling method name
     * @return instance properties object.
     */
    public InstanceProperties addFloatPropertyToInstance(String             sourceName,
                                                         InstanceProperties properties,
                                                         String             propertyName,
                                                         float              propertyValue,
                                                         String             methodName)
    {
        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

        primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_FLOAT);
        primitivePropertyValue.setPrimitiveValue(propertyValue);

        resultingProperties.setProperty(propertyName, primitivePropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName  name of caller
     * @param properties  properties object to add property to may be null.
     * @param propertyName  name of property
     * @param propertyValue  value of property
     * @param methodName  calling method name
     * @return instance properties object.
     */
    public InstanceProperties addDatePropertyToInstance(String             sourceName,
                                                        InstanceProperties properties,
                                                        String             propertyName,
                                                        Date               propertyValue,
                                                        String             methodName)
    {


        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

        /*
         * Date objects are stored in PrimitivePropertyValue as Java Long.
         */
        primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE);
        Long longValue = propertyValue.getTime();
        primitivePropertyValue.setPrimitiveValue(longValue);

        resultingProperties.setProperty(propertyName, primitivePropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param propertyValue value of property
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addBooleanPropertyToInstance(String             sourceName,
                                                           InstanceProperties properties,
                                                           String             propertyName,
                                                           boolean            propertyValue,
                                                           String             methodName)
    {
        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

        primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN);
        primitivePropertyValue.setPrimitiveValue(propertyValue);
        primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN.getName());
        primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN.getGUID());

        resultingProperties.setProperty(propertyName, primitivePropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied property to an instance properties object.  If the instance property object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param ordinal numeric value of property
     * @param symbolicName String value of property
     * @param description String description of property value
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addEnumPropertyToInstance(String             sourceName,
                                                        InstanceProperties properties,
                                                        String             propertyName,
                                                        int                ordinal,
                                                        String             symbolicName,
                                                        String             description,
                                                        String             methodName)
    {
        InstanceProperties  resultingProperties;

        log.debug("Adding property " + propertyName + " for " + methodName);

        if (properties == null)
        {
            log.debug("First property");

            resultingProperties = new InstanceProperties();
        }
        else
        {
            resultingProperties = properties;
        }


        EnumPropertyValue enumPropertyValue = new EnumPropertyValue();

        enumPropertyValue.setOrdinal(ordinal);
        enumPropertyValue.setSymbolicName(symbolicName);
        enumPropertyValue.setDescription(description);

        resultingProperties.setProperty(propertyName, enumPropertyValue);

        return resultingProperties;
    }


    /**
     * Add the supplied array property to an instance properties object.  The supplied array is stored as a single
     * property in the instances properties.   If the instance properties object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param arrayValues contents of the array
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addStringArrayPropertyToInstance(String              sourceName,
                                                               InstanceProperties  properties,
                                                               String              propertyName,
                                                               List<String>        arrayValues,
                                                               String              methodName)
    {
        if ((arrayValues != null) && (! arrayValues.isEmpty()))
        {
            log.debug("Adding property " + propertyName + " for " + methodName + " from " + sourceName);

            InstanceProperties  resultingProperties;

            if (properties == null)
            {
                resultingProperties = new InstanceProperties();
            }
            else
            {
                resultingProperties = properties;
            }

            ArrayPropertyValue arrayPropertyValue = new ArrayPropertyValue();
            arrayPropertyValue.setArrayCount(arrayValues.size());
            int index = 0;
            for (String arrayValue : arrayValues )
            {
                PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();

                primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING);
                primitivePropertyValue.setPrimitiveValue(arrayValue);

                arrayPropertyValue.setArrayValue(index, primitivePropertyValue);
                index++;
            }

            resultingProperties.setProperty(propertyName, arrayPropertyValue);

            log.debug("Returning instanceProperty: " + resultingProperties.toString());

            return resultingProperties;
        }

        log.debug("Null property");
        return properties;
    }


    /**
     * Add the supplied map property to an instance properties object.  The supplied map is stored as a single
     * property in the instances properties.   If the instance properties object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param mapValues contents of the map
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addMapPropertyToInstance(String              sourceName,
                                                       InstanceProperties  properties,
                                                       String              propertyName,
                                                       Map<String, Object> mapValues,
                                                       String              methodName)
    {
        if (mapValues != null)
        {
            log.debug("Adding property " + propertyName + " for " + methodName);

            if ((mapValues != null) && (! mapValues.isEmpty()))
            {
                InstanceProperties  resultingProperties;

                if (properties == null)
                {
                    resultingProperties = new InstanceProperties();
                }
                else
                {
                    resultingProperties = properties;
                }


                /*
                 * The values of a map property are stored as an embedded InstanceProperties object.
                 */
                InstanceProperties  mapInstanceProperties  = this.addPropertyMapToInstance(sourceName,
                                                                                           null,
                                                                                           mapValues,
                                                                                           methodName);

                /*
                 * If there was content in the map then the resulting InstanceProperties are added as
                 * a property to the resulting properties.
                 */
                if (mapInstanceProperties != null)
                {
                    MapPropertyValue mapPropertyValue = new MapPropertyValue();
                    mapPropertyValue.setMapValues(mapInstanceProperties);
                    resultingProperties.setProperty(propertyName, mapPropertyValue);

                    log.debug("Returning instanceProperty: " + resultingProperties.toString());

                    return resultingProperties;
                }
            }
        }

        log.debug("Null property");
        return properties;
    }


    /**
     * Add the supplied map property to an instance properties object.  The supplied map is stored as a single
     * property in the instances properties.   If the instance properties object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param mapValues contents of the map
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addStringMapPropertyToInstance(String              sourceName,
                                                             InstanceProperties  properties,
                                                             String              propertyName,
                                                             Map<String, String> mapValues,
                                                             String              methodName)
    {
        if (mapValues != null)
        {
            log.debug("Adding property " + propertyName + " for " + methodName);

            if ((mapValues != null) && (! mapValues.isEmpty()))
            {
                InstanceProperties  resultingProperties;

                if (properties == null)
                {
                    resultingProperties = new InstanceProperties();
                }
                else
                {
                    resultingProperties = properties;
                }


                /*
                 * The values of a map property are stored as an embedded InstanceProperties object.
                 */
                InstanceProperties  mapInstanceProperties  = this.addStringPropertyMapToInstance(sourceName,
                                                                                                 null,
                                                                                                 propertyName,
                                                                                                 mapValues,
                                                                                                 methodName);

                /*
                 * If there was content in the map then the resulting InstanceProperties are added as
                 * a property to the resulting properties.
                 */
                if (mapInstanceProperties != null)
                {
                    MapPropertyValue mapPropertyValue = new MapPropertyValue();
                    mapPropertyValue.setMapValues(mapInstanceProperties);
                    resultingProperties.setProperty(propertyName, mapPropertyValue);

                    log.debug("Returning instanceProperty: " + resultingProperties.toString());

                    return resultingProperties;
                }
            }
        }

        log.debug("Null property");
        return properties;
    }



    /**
     * Add the supplied property map to an instance properties object.  Each of the entries in the map is added
     * as a separate property in instance properties.  If the instance properties object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param mapValues contents of the map
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addPropertyMapToInstance(String              sourceName,
                                                       InstanceProperties  properties,
                                                       Map<String, Object> mapValues,
                                                       String              methodName)
    {
        if ((mapValues != null) && (! mapValues.isEmpty()))
        {
            log.debug("Building map property for " + methodName);

            InstanceProperties  resultingProperties;

            if (properties == null)
            {
                resultingProperties = new InstanceProperties();
            }
            else
            {
                resultingProperties = properties;
            }

            int propertyCount = 0;

            for (String mapPropertyName : mapValues.keySet())
            {
                Object mapPropertyValue = mapValues.get(mapPropertyName);

                if (mapPropertyValue instanceof String)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING);
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getGUID());
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Integer)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_INT.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Long)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_LONG);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_LONG.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_LONG.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Short)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_SHORT);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_SHORT.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_SHORT.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Date)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DATE.getGUID());
                    /*
                     * Internally, dates are stored as Java Long.
                     */
                    Long timestamp = ((Date) mapPropertyValue).getTime();
                    primitivePropertyValue.setPrimitiveValue(timestamp);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Character)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_CHAR);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_CHAR.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_CHAR.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Byte)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BYTE);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BYTE.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BYTE.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Boolean)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BOOLEAN.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Float)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_FLOAT);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_FLOAT.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_FLOAT.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof BigDecimal)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(
                            PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGDECIMAL);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGDECIMAL.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGDECIMAL.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof BigInteger)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(
                            PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGINTEGER);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGINTEGER.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_BIGINTEGER.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else if (mapPropertyValue instanceof Double)
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DOUBLE);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DOUBLE.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_DOUBLE.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
                else
                {
                    PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                    primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_UNKNOWN);
                    primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_UNKNOWN.getName());
                    primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_UNKNOWN.getGUID());
                    primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                    resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                    propertyCount++;
                }
            }

            if (propertyCount > 0)
            {
                log.debug("Returning instanceProperty: " + resultingProperties.toString());

                return resultingProperties;
            }
        }

        log.debug("Null property");
        return properties;
    }


    /**
     * Add the supplied property map to an instance properties object.  Each of the entries in the map is added
     * as a separate property in instance properties.  If the instance properties object
     * supplied is null, a new instance properties object is created.
     *
     * @param sourceName name of caller
     * @param properties properties object to add property to, may be null.
     * @param propertyName name of property
     * @param mapValues contents of the map
     * @param methodName calling method name
     * @return instance properties object.
     */
    public InstanceProperties addStringPropertyMapToInstance(String              sourceName,
                                                             InstanceProperties  properties,
                                                             String              propertyName,
                                                             Map<String, String> mapValues,
                                                             String              methodName)
    {
        if ((mapValues != null) && (! mapValues.isEmpty()))
        {
            log.debug("Adding property " + propertyName + " for " + methodName);

            InstanceProperties  resultingProperties;

            if (properties == null)
            {
                resultingProperties = new InstanceProperties();
            }
            else
            {
                resultingProperties = properties;
            }

            int propertyCount = 0;

            for (String mapPropertyName : mapValues.keySet())
            {
                String mapPropertyValue = mapValues.get(mapPropertyName);

                PrimitivePropertyValue primitivePropertyValue = new PrimitivePropertyValue();
                primitivePropertyValue.setPrimitiveDefCategory(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING);
                primitivePropertyValue.setPrimitiveValue(mapPropertyValue);
                primitivePropertyValue.setTypeName(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getName());
                primitivePropertyValue.setTypeGUID(PrimitiveDefCategory.OM_PRIMITIVE_TYPE_STRING.getGUID());
                resultingProperties.setProperty(mapPropertyName, primitivePropertyValue);
                propertyCount++;
            }

            if (propertyCount > 0)
            {
                log.debug("Returning instanceProperty: " + resultingProperties.toString());

                return resultingProperties;
            }
        }

        log.debug("Null property");
        return properties;
    }


    /**
     * Verify that a TypeDefPatch is not null and is for a recognized type.
     *
     * @param sourceName source of the request (used for logging)
     * @param typeDefPatch typeDefPatch to test
     * @param methodName calling method
     * @throws InvalidParameterException the original typeDef or typeDefPatch is null
     * @throws PatchErrorException the typeDefPatch is invalid
     */
    public void validateTypeDefPatch(String       sourceName,
                                     TypeDefPatch typeDefPatch,
                                     String       methodName) throws InvalidParameterException,
                                                                        PatchErrorException
    {
        final String  thisMethodName = "validateTypeDefPatch";



        if (typeDefPatch == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF_PATCH;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                            sourceName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                thisMethodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }


        if (typeDefPatch.getUpdateToVersion() <= typeDefPatch.getApplyToVersion())
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.INVALID_PATCH_VERSION;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                            sourceName,
                                                                                                            Long.toString(typeDefPatch.getApplyToVersion()),
                                                                                                            Long.toString(typeDefPatch.getUpdateToVersion()),
                                                                                                            typeDefPatch.toString());

            throw new PatchErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          methodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
        }

        if (typeDefPatch.getNewVersionName() == null)
        {
            logNullMandatoryPatchField(sourceName, typeDefPatch, "newVersionName", methodName);
        }

        if (typeDefPatch.getUpdatedBy() == null)
        {
            logNullMandatoryPatchField(sourceName, typeDefPatch, "updatedBy", methodName);
        }

        if (typeDefPatch.getUpdateTime() == null)
        {
            logNullMandatoryPatchField(sourceName, typeDefPatch, "updatedTime", methodName);
        }
    }


    /**
     * Report a null field in a TypeDefPatch that is actually mandatory.
     *
     * @param sourceName source of the TypeDef
     * @param typeDefPatch patch in error
     * @param fieldName null field name
     * @param methodName calling method
     * @throws PatchErrorException resulting exception
     */
    private void logNullMandatoryPatchField(String       sourceName,
                                            TypeDefPatch typeDefPatch,
                                            String       fieldName,
                                            String       methodName) throws PatchErrorException
    {
        OMRSErrorCode errorCode    = OMRSErrorCode.NULL_MANDATORY_PATCH_FIELD;
        String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                        sourceName,
                                                                                                        fieldName,
                                                                                                        typeDefPatch.toString());

        throw new PatchErrorException(errorCode.getHTTPErrorCode(),
                                      this.getClass().getName(),
                                      methodName,
                                      errorMessage,
                                      errorCode.getSystemAction(),
                                      errorCode.getUserAction());
    }


    /**
     * Returns an updated TypeDef that has had the supplied patch applied.  It throws an exception if any part of
     * the patch is incompatible with the original TypeDef.  For example, if there is a mismatch between
     * the type or version that either represents.
     *
     * @param sourceName      source of the TypeDef (used for logging)
     * @param originalTypeDef typeDef to update
     * @param typeDefPatch    patch to apply
     * @param methodName      calling method
     * @return updated TypeDef
     * @throws InvalidParameterException the original typeDef or typeDefPatch is null
     * @throws PatchErrorException  the patch is either badly formatted, or does not apply to the supplied TypeDef
     */
    public TypeDef applyPatch(String       sourceName,
                              TypeDef      originalTypeDef,
                              TypeDefPatch typeDefPatch,
                              String       methodName) throws InvalidParameterException, PatchErrorException
    {
        final String  thisMethodName = "applyPatch";
        final String  typeDefParameterName = "originalTypeDef";

        this.validateTypeDefPatch(sourceName, typeDefPatch, methodName);

        if (originalTypeDef == null)
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.NULL_TYPEDEF;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(typeDefParameterName,
                                                                                                            methodName,
                                                                                                            sourceName);

            throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                                this.getClass().getName(),
                                                thisMethodName,
                                                errorMessage,
                                                errorCode.getSystemAction(),
                                                errorCode.getUserAction());
        }

        TypeDef updatedTypeDef  = originalTypeDef.cloneFromSubclass();

        if (originalTypeDef.getVersion() == typeDefPatch.getApplyToVersion())
        {
            updatedTypeDef.setVersion(typeDefPatch.getUpdateToVersion());
            updatedTypeDef.setVersionName(typeDefPatch.getNewVersionName());
            updatedTypeDef.setUpdatedBy(typeDefPatch.getUpdatedBy());
            updatedTypeDef.setUpdateTime(typeDefPatch.getUpdateTime());

            if (typeDefPatch.getDescription() != null)
            {
                updatedTypeDef.setDescription(typeDefPatch.getDescription());
            }

            if (typeDefPatch.getDescriptionGUID() != null)
            {
                updatedTypeDef.setDescriptionGUID(typeDefPatch.getDescriptionGUID());
            }

            if (typeDefPatch.getPropertyDefinitions() != null)
            {
                /*
                 * New attributes have been defined - or existing ones updated.
                 */
                List<TypeDefAttribute> existingProperties = originalTypeDef.getPropertiesDefinition();

                if (existingProperties == null)
                {
                    updatedTypeDef.setPropertiesDefinition(typeDefPatch.getPropertyDefinitions());
                }
                else
                {
                    /*
                     * Using a map to ensure no duplicate definitions for a property occur
                     * in the resulting property list.
                     */
                    Map<String, TypeDefAttribute> newProperties = new HashMap<>();

                    for (TypeDefAttribute propertyDefinition : existingProperties)
                    {
                        /*
                         * The existing properties are initially preserved.  The new properties from the
                         * patch will be added over the top as long as they are compatible.
                         */
                        if (propertyDefinition != null)
                        {
                            newProperties.put(propertyDefinition.getAttributeName(), propertyDefinition);
                        }
                    }

                    for (TypeDefAttribute newPropertyDefinition : typeDefPatch.getPropertyDefinitions())
                    {
                        if (newPropertyDefinition != null)
                        {
                            String newPropertyName = newPropertyDefinition.getAttributeName();

                            if (newPropertyName != null)
                            {
                                TypeDefAttribute oldPropertyDefinition = newProperties.put(newPropertyName, newPropertyDefinition);

                                if (oldPropertyDefinition != null)
                                {
                                    /*
                                     * The type of the property must not change.  Note we trust that the current type is valid but not the patch.
                                     */
                                    if (! oldPropertyDefinition.getAttributeType().equals(newPropertyDefinition.getAttributeType()))
                                    {
                                        String newPropertyType = "<null>";

                                        if (newPropertyDefinition.getAttributeType() != null)
                                        {
                                            newPropertyType = newPropertyDefinition.getAttributeType().toString();
                                        }

                                        OMRSErrorCode errorCode    = OMRSErrorCode.INCOMPATIBLE_PROPERTY_PATCH;
                                        String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                                                        sourceName,
                                                                                                                                        newPropertyName,
                                                                                                                                        oldPropertyDefinition.getAttributeType().toString(),
                                                                                                                                        newPropertyType,
                                                                                                                                        typeDefPatch.toString());

                                        throw new PatchErrorException(errorCode.getHTTPErrorCode(),
                                                                      this.getClass().getName(),
                                                                      methodName,
                                                                      errorMessage,
                                                                      errorCode.getSystemAction(),
                                                                      errorCode.getUserAction());
                                    }
                                }
                            }
                        }
                    }

                    updatedTypeDef.setPropertiesDefinition(new ArrayList<>(newProperties.values()));
                }
            }

            if (typeDefPatch.getTypeDefOptions() != null)
            {
                updatedTypeDef.setOptions(typeDefPatch.getTypeDefOptions());
            }

            if (typeDefPatch.getExternalStandardMappings() != null)
            {
                updatedTypeDef.setExternalStandardMappings(typeDefPatch.getExternalStandardMappings());
            }

            if (typeDefPatch.getValidInstanceStatusList() != null)
            {
                updatedTypeDef.setValidInstanceStatusList(typeDefPatch.getValidInstanceStatusList());
            }

            if (typeDefPatch.getInitialStatus() != null)
            {
                updatedTypeDef.setInitialStatus(typeDefPatch.getInitialStatus());
            }

            /*
             * OK to perform the update.  Need to create a new TypeDef object.  TypeDef is an abstract class
             * so need to use the TypeDefCategory to create a new object of the correct type.
             */
            TypeDefCategory category = originalTypeDef.getCategory();

            try
            {
                switch (category)
                {
                    case ENTITY_DEF:
                        break;

                    case RELATIONSHIP_DEF:
                        RelationshipDef relationshipDef = (RelationshipDef) updatedTypeDef;
                        if (typeDefPatch.getEndDef1() != null)
                        {
                            relationshipDef.setEndDef1(typeDefPatch.getEndDef1());
                        }
                        if (typeDefPatch.getEndDef2() != null)
                        {
                            relationshipDef.setEndDef2(typeDefPatch.getEndDef2());
                        }
                        break;

                    case CLASSIFICATION_DEF:
                        ClassificationDef classificationDef = (ClassificationDef) updatedTypeDef;
                        if (typeDefPatch.getValidEntityDefs() != null)
                        {
                            classificationDef.setValidEntityDefs(typeDefPatch.getValidEntityDefs());
                        }
                        break;
                }
            }
            catch (ClassCastException castError)
            {
                throwHelperLogicError(sourceName, methodName, thisMethodName, castError);
            }

            return updatedTypeDef;
        }
        else if (typeDefPatch.getApplyToVersion() < originalTypeDef.getVersion())
        {
            /*
             * The patch has already been applied and so can be ignored.  This is not an
             * error because all members of the cohort broadcast new types so it is to be
             * expected that the same patch will come in multiple times.
             */
            return originalTypeDef;
        }
        else
        {
            OMRSErrorCode errorCode    = OMRSErrorCode.INCOMPATIBLE_PATCH_VERSION;
            String        errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                            sourceName,
                                                                                                            Long.toString(typeDefPatch.getApplyToVersion()),
                                                                                                            Long.toString(originalTypeDef.getVersion()),
                                                                                                            typeDefPatch.toString());

            throw new PatchErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          methodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
        }
    }


    /**
     * Throws a logic error exception when the repository helper is called with invalid parameters.
     * Normally this means the repository helper methods have been called in the wrong order.
     *
     * @param sourceName name of the calling repository or service
     * @param originatingMethodName method that called the repository validator
     * @param localMethodName local method that deleted the error
     */
    private void throwHelperLogicError(String     sourceName,
                                       String     originatingMethodName,
                                       String     localMethodName)
    {
        OMRSErrorCode errorCode = OMRSErrorCode.HELPER_LOGIC_ERROR;
        String errorMessage     = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     localMethodName,
                                                                                                     originatingMethodName);

        throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          localMethodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
    }


    /**
     * Throws a logic error exception when the repository helper is called with invalid parameters.
     * Normally this means the repository helper methods have been called in the wrong order.
     *
     * @param sourceName name of the calling repository or service
     * @param originatingMethodName method that called the repository validator
     * @param localMethodName local method that deleted the error
     * @param unexpectedException unexpected exception caught by the helper logic
     */
    private void throwHelperLogicError(String     sourceName,
                                       String     originatingMethodName,
                                       String     localMethodName,
                                       Throwable  unexpectedException)
    {
        OMRSErrorCode errorCode = OMRSErrorCode.HELPER_LOGIC_EXCEPTION;
        String errorMessage     = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     localMethodName,
                                                                                                     originatingMethodName);

        throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          localMethodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction(),
                                          unexpectedException);
    }
}
