/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.metadatasecurity.connectors;

import org.odpi.openmetadata.frameworks.connectors.ConnectorBase;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.PropertyServerException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.UserNotAuthorizedException;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Asset;
import org.odpi.openmetadata.frameworks.connectors.properties.beans.Connection;
import org.odpi.openmetadata.metadatasecurity.OpenMetadataAssetSecurity;
import org.odpi.openmetadata.metadatasecurity.OpenMetadataConnectionSecurity;
import org.odpi.openmetadata.metadatasecurity.OpenMetadataServerSecurity;
import org.odpi.openmetadata.metadatasecurity.OpenMetadataServiceSecurity;
import org.odpi.openmetadata.metadatasecurity.ffdc.OpenMetadataSecurityAuditCode;
import org.odpi.openmetadata.metadatasecurity.ffdc.OpenMetadataSecurityErrorCode;
import org.odpi.openmetadata.metadatasecurity.properties.AssetAuditHeader;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.auditable.AuditableConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.OpenMetadataRepositorySecurity;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.TypeDef;

import java.util.List;

/**
 * OpenMetadataServerSecurityConnector provides the base class for an Open Metadata Security Connector for
 * a server.  This connector is configured in an OMAG Configuration Document.  Its default behavior
 * is to reject every request.  It generates well-defined exceptions and audit log
 * messages.
 *
 * Override these to define the required access for the deployment environment.  The methods
 * in this base class can be called if access is to be denied as a way of making use of the message
 * logging and exceptions.
 */
public class OpenMetadataServerSecurityConnector extends ConnectorBase implements AuditableConnector,
                                                                                  OpenMetadataRepositorySecurity,
                                                                                  OpenMetadataServerSecurity,
                                                                                  OpenMetadataServiceSecurity,
                                                                                  OpenMetadataConnectionSecurity,
                                                                                  OpenMetadataAssetSecurity
{
    protected  OMRSAuditLog  auditLog = null;
    protected  String        serverName = null;
    protected  String        localServerUserId = null;
    protected  String        connectorName = null;


    /**
     * Write an audit log message to say that the connector is initializing.
     */
    protected void logConnectorStarting()
    {
        if (auditLog != null)
        {
            final String                  actionDescription = "start";
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.SERVICE_INITIALIZING;
            auditLog.logRecord(actionDescription,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(connectorName, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }
    }


    /**
     * Write an audit log message to say that the connector is stopping.
     */
    protected void logConnectorDisconnecting()
    {
        if (auditLog != null)
        {
            final String                  actionDescription = "disconnect";
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.SERVICE_SHUTDOWN;
            auditLog.logRecord(actionDescription,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(connectorName, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }
    }


    /**
     * Return a string representing the unique identifier for the asset.
     * If the asset is null then the guid is "null", if the guid is null then
     * the result is "null-guid".
     *
     * @param asset asset to test
     * @return string identifier for messages
     */
    protected  String  getAssetGUID(Asset  asset)
    {
        if (asset == null)
        {
            return "<null>";
        }

        if (asset.getGUID() == null)
        {
            return "<null-guid>";
        }

        return asset.getGUID();
    }



    /**
     * Return a string representing the list of zones.
     *
     * @param zones zones to output
     * @return string for messages
     */
    protected  String  printZoneList(List<String>   zones)
    {
        if (zones == null)
        {
            return "<null>";
        }

        if (zones.isEmpty())
        {
            return "[]";
        }

        return zones.toString();
    }


    /**
     * Return a string representing the unique identifier for the connection.
     * If the connection is null then the guid is "null", if the guid is null then
     * the result is "null-name".
     *
     * @param connection connection to test
     * @return string identifier for messages
     */
    protected  String  getConnectionQualifiedName(Connection  connection)
    {
        if (connection == null)
        {
            return "<null>";
        }

        if (connection.getQualifiedName() == null)
        {
            return "<null-name>";
        }

        return connection.getQualifiedName();
    }



    /**
     * Return a string representing the unique identifier for a repository instance.
     * If the instance is null then the guid is "null", if the guid is null then
     * the result is "null-guid".
     *
     * @param instance instance to test
     * @return string identifier for messages
     */
    protected  String  getInstanceGUID(InstanceHeader instance)
    {
        if (instance == null)
        {
            return "<null>";
        }

        if (instance.getGUID() == null)
        {
            return "<null-guid>";
        }

        return instance.getGUID();
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedServerAccess(String   userId,
                                                 String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_SERVER_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_SERVER_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId, serverName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param serviceName name of service
     * @param serviceOperationName name of operation
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedServiceAccess(String   userId,
                                                  String   serviceName,
                                                  String   serviceOperationName,
                                                  String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_SERVICE_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, serviceOperationName, serviceName, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_SERVICE_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId, serviceOperationName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param typeGUID uniqueId of type
     * @param typeName name of type
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedTypeAccess(String   userId,
                                               String   typeGUID,
                                               String   typeName,
                                               String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_TYPE_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, typeName, typeGUID, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_TYPE_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId, typeName, typeGUID, serverName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param typeGUID uniqueId of type
     * @param typeName name of type
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedTypeChange(String   userId,
                                               String   typeGUID,
                                               String   typeName,
                                               String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_TYPE_CHANGE;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, typeName, typeGUID, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_TYPE_CHANGE;
        String                        errorMessage = errorCode.getErrorMessageId()
                + errorCode.getFormattedErrorMessage(userId, typeName, typeGUID, serverName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param instanceGUID uniqueId of type
     * @param typeName name of type
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedInstanceAccess(String   userId,
                                                   String   instanceGUID,
                                                   String   typeName,
                                                   String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_INSTANCE_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, instanceGUID, typeName, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_INSTANCE_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        instanceGUID,
                                                                                        typeName,
                                                                                        serverName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param instanceGUID uniqueId of type
     * @param typeName name of type
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedInstanceChange(String   userId,
                                                   String   instanceGUID,
                                                   String   typeName,
                                                   String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_INSTANCE_CHANGE;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, instanceGUID, typeName, serverName),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_INSTANCE_CHANGE;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        instanceGUID,
                                                                                        typeName,
                                                                                        serverName);

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param asset asset being accessed
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedAssetAccess(String   userId,
                                                Asset    asset,
                                                String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_ASSET_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, this.getAssetGUID(asset)),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_ASSET_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId, this.getAssetGUID(asset));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param asset asset being accessed
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedAssetChange(String   userId,
                                                Asset    asset,
                                                String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_ASSET_CHANGE;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, this.getAssetGUID(asset)),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_ASSET_CHANGE;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        this.getAssetGUID(asset));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param asset asset being accessed
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwIncompleteAsset(String   userId,
                                        Asset    asset,
                                        String   methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.INCOMPLETE_ASSET;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, this.getAssetGUID(asset)),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.INCOMPLETE_ASSET;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        this.getAssetGUID(asset));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param asset asset being accessed
     * @param originalZones previous value of the zone membership for the asset being accessed
     * @param newZones new value of the zone membership for the asset being accessed
     * @param methodName calling method
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedZoneChange(String        userId,
                                               Asset         asset,
                                               List<String>  originalZones,
                                               List<String>  newZones,
                                               String        methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_ZONE_CHANGE;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId,
                                                                this.getAssetGUID(asset),
                                                                this.printZoneList(originalZones),
                                                                this.printZoneList(newZones)),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_ZONE_CHANGE;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        this.getAssetGUID(asset),
                                                                                        this.printZoneList(originalZones),
                                                                                        this.printZoneList(newZones));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param asset asset in error
     * @param methodName calling method
     * @throws UserNotAuthorizedException the user is not authorized to access this zone
     */
    protected void throwUnauthorizedAssetFeedback(String       userId,
                                                  Asset        asset,
                                                  String       methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_ASSET_FEEDBACK;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId,
                                                                this.getAssetGUID(asset)),
                               null,
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_ASSET_FEEDBACK;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId,
                                                                                        this.getAssetGUID(asset));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Write an audit log message and throw exception to record an
     * unauthorized access.
     *
     * @param userId calling user
     * @param connection connection to validate
     * @param methodName calling method
     *
     * @throws UserNotAuthorizedException the authorization check failed
     */
    protected void throwUnauthorizedConnectionAccess(String       userId,
                                                     Connection   connection,
                                                     String       methodName) throws UserNotAuthorizedException
    {
        if (auditLog != null)
        {
            OpenMetadataSecurityAuditCode auditCode;

            auditCode = OpenMetadataSecurityAuditCode.UNAUTHORIZED_SERVICE_ACCESS;
            auditLog.logRecord(methodName,
                               auditCode.getLogMessageId(),
                               auditCode.getSeverity(),
                               auditCode.getFormattedLogMessage(userId, this.getConnectionQualifiedName(connection)),
                               connection.toString(),
                               auditCode.getSystemAction(),
                               auditCode.getUserAction());
        }

        OpenMetadataSecurityErrorCode errorCode = OpenMetadataSecurityErrorCode.UNAUTHORIZED_SERVICE_ACCESS;
        String                        errorMessage = errorCode.getErrorMessageId()
                                                   + errorCode.getFormattedErrorMessage(userId, this.getConnectionQualifiedName(connection));

        throw new UserNotAuthorizedException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction(),
                                             userId);
    }


    /**
     * Receive an audit log object that can be used to record audit log messages.  The caller has initialized it
     * with the correct component description and log destinations.
     *
     * @param auditLog audit log object
     */
    public void setAuditLog(OMRSAuditLog auditLog)
    {
        this.auditLog = auditLog;
    }


    /**
     * Set the name of the server that this connector is supporting.
     *
     * @param serverName name of server
     */
    public void  setServerName(String   serverName)
    {
        this.serverName = serverName;
    }


    /**
     * Provide the local server's userId.  This is used for requests that originate from within the local
     * server.
     *
     * @param userId local server's userId
     */
    public void setLocalServerUserId(String    userId)
    {
        this.localServerUserId = userId;
    }


    /**
     * Indicates that the connector is completely configured and can begin processing.
     *
     * @throws ConnectorCheckedException there is a problem within the connector.
     */
    public void start() throws ConnectorCheckedException
    {
        super.start();

        connectorName = this.getClass().getName();
        logConnectorStarting();
    }


    /**
     * Determine the appropriate setting for the asset zones depending on the content of the asset and the
     * default zones.  This is called whenever a new asset is created.
     *
     * The default behavior is to use the default values, unless the zones have been explicitly set up,
     * in which case, they are left unchanged.
     *
     * @param defaultZones setting of the default zones for the service
     * @param asset initial values for the asset
     *
     * @return list of zones to set in the asset
     * @throws InvalidParameterException one of the asset values is invalid
     * @throws PropertyServerException there is a problem calculating the zones
     */
    public List<String> initializeAssetZones(List<String>  defaultZones,
                                             Asset         asset) throws InvalidParameterException,
                                                                         PropertyServerException
    {
        List<String>  resultingZones = null;

        if (asset != null)
        {
            if ((asset.getZoneMembership() == null) || (asset.getZoneMembership().isEmpty()))
            {
                resultingZones = defaultZones;
            }
            else
            {
                resultingZones = asset.getZoneMembership();
            }
        }

        return resultingZones;
    }


    /**
     * Determine the appropriate setting for the asset zones depending on the content of the asset and the
     * settings of both default zones and supported zones.  This method is called whenever an asset's
     * values are changed.
     *
     * The default behavior is to keep the updated zones as they are.
     *
     * @param defaultZones setting of the default zones for the service
     * @param supportedZones setting of the supported zones for the service
     * @param originalAsset original values for the asset
     * @param updatedAsset updated values for the asset
     *
     * @return list of zones to set in the asset
     * @throws InvalidParameterException one of the asset values is invalid
     * @throws PropertyServerException there is a problem calculating the zones
     */
    public List<String> verifyAssetZones(List<String>  defaultZones,
                                         List<String>  supportedZones,
                                         Asset         originalAsset,
                                         Asset         updatedAsset) throws InvalidParameterException,
                                                                            PropertyServerException
    {
        List<String>  resultingZones = null;

        if (updatedAsset != null)
        {
            resultingZones = updatedAsset.getZoneMembership();
        }

        return resultingZones;
    }


    /**
     * Check that the calling user is authorized to issue a (any) request to the OMAG Server Platform.
     *
     * @param userId calling user
     *
     * @throws UserNotAuthorizedException the user is not authorized to access this function
     */
    public void  validateUserForServer(String   userId) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForServer";

        throwUnauthorizedServerAccess(userId, methodName);
    }


    /**
     * Check that the calling user is authorized to update the configuration for a server.
     *
     * @param userId calling user
     *
     * @throws UserNotAuthorizedException the user is not authorized to change configuration
     */
    public void  validateUserAsServerAdmin(String   userId) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserAsServerAdmin";
        final String  serviceName = "Administration Services";
        final String  serviceOperationName = "configuration";

        throwUnauthorizedServiceAccess(userId, serviceName, serviceOperationName, methodName);
    }


    /**
     * Check that the calling user is authorized to issue operator requests to the OMAG Server.
     *
     * @param userId calling user
     *
     * @throws UserNotAuthorizedException the user is not authorized to issue operator commands to this server
     */
    public void  validateUserAsServerOperator(String   userId) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserAsServerOperator";
        final String  serviceName = "Administration Services";
        final String  serviceOperationName = "operations";

        throwUnauthorizedServiceAccess(userId, serviceName, serviceOperationName, methodName);
    }


    /**
     * Check that the calling user is authorized to issue operator requests to the OMAG Server.
     *
     * @param userId calling user
     *
     * @throws UserNotAuthorizedException the user is not authorized to issue diagnostic commands to this server
     */
    public void  validateUserAsServerInvestigator(String   userId) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserAsServerInvestigator";
        final String  serviceName = "Administration Services";
        final String  serviceOperationName = "query";

        throwUnauthorizedServiceAccess(userId, serviceName, serviceOperationName, methodName);
    }


    /**
     * Check that the calling user is authorized to issue this request.
     *
     * @param userId calling user
     * @param serviceName name of called service
     *
     * @throws UserNotAuthorizedException the user is not authorized to access this service
     */
    public void  validateUserForService(String   userId,
                                        String   serviceName) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForService";
        final String  serviceOperationName = "any";

        throwUnauthorizedServiceAccess(userId, serviceName, serviceOperationName, methodName);
    }


    /**
     * Check that the calling user is authorized to issue this specific request.
     *
     * @param userId calling user
     * @param serviceName name of called service
     * @param serviceOperationName name of called operation
     *
     * @throws UserNotAuthorizedException the user is not authorized to access this service
     */
    public void  validateUserForServiceOperation(String   userId,
                                                 String   serviceName,
                                                 String   serviceOperationName) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForServiceOperation";

        throwUnauthorizedServiceAccess(userId, serviceName, serviceOperationName, methodName);
    }

    /**
     * Tests for whether a specific user should have access to a connection.
     *
     * @param userId identifier of user
     * @param connection connection object
     * @throws UserNotAuthorizedException the user is not authorized to access this service
     */
    public void  validateUserForConnection(String     userId,
                                           Connection connection) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForConnection";

        throwUnauthorizedConnectionAccess(userId, connection, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to create an asset within a zone.
     *
     * @param userId identifier of user
     * @param asset asset details
     * @throws UserNotAuthorizedException the user is not authorized to change this asset
     */
    public void  validateUserForAssetCreate(String     userId,
                                            Asset      asset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetCreate";

        throwUnauthorizedAssetChange(userId, asset, methodName);
    }


    /**
     * Tests for whether a specific user should have read access to a specific asset within a zone.
     *
     * @param userId identifier of user
     * @param asset asset to test
     * @throws UserNotAuthorizedException the user is not authorized to access this asset
     */
    public void  validateUserForAssetRead(String     userId,
                                          Asset      asset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetRead";

        throwUnauthorizedAssetAccess(userId, asset, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to update an asset.
     * This is used for a general asset update, which may include changes to the
     * zones and the ownership.
     *
     * @param userId identifier of user
     * @param originalAsset original asset details
     * @param originalAssetAuditHeader details of the asset's audit header
     * @param newAsset new asset details
     * @throws UserNotAuthorizedException the user is not authorized to change this asset
     */
    public void  validateUserForAssetDetailUpdate(String           userId,
                                                  Asset            originalAsset,
                                                  AssetAuditHeader originalAssetAuditHeader,
                                                  Asset            newAsset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetDetailUpdate";

        throwUnauthorizedAssetChange(userId, originalAsset, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to update elements attached directly
     * to an asset such as schema and connections.
     *
     * @param userId identifier of user
     * @param asset original asset details
     * @throws UserNotAuthorizedException the user is not authorized to change this asset
     */
    public void  validateUserForAssetAttachmentUpdate(String     userId,
                                                      Asset      asset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetAttachmentUpdate";

        throwUnauthorizedAssetChange(userId, asset, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to attach feedback - such as comments,
     * ratings, tags and likes, to the asset.
     *
     * @param userId identifier of user
     * @param asset original asset details
     * @throws UserNotAuthorizedException the user is not authorized to change this asset
     */
    public void  validateUserForAssetFeedback(String     userId,
                                              Asset      asset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetFeedback";

        throwUnauthorizedAssetFeedback(userId, asset, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to delete an asset within a zone.
     *
     * @param userId identifier of user
     * @param asset asset details
     * @throws UserNotAuthorizedException the user is not authorized to change this asset
     */
    public void  validateUserForAssetDelete(String     userId,
                                            Asset      asset) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForAssetDelete";

        throwUnauthorizedAssetChange(userId, asset, methodName);
    }


    /**
     * Tests for whether a specific user should have the right to create a typeDef within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param typeDef typeDef details
     * @throws UserNotAuthorizedException the user is not authorized to maintain types
     */
    public void  validateUserForTypeCreate(String     userId,
                                           String     metadataCollectionName,
                                           TypeDef    typeDef) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForTypeCreate";

        if (typeDef != null)
        {
            throwUnauthorizedTypeChange(userId, typeDef.getGUID(), typeDef.getName(), methodName);
        }
    }


    /**
     * Tests for whether a specific user should have read access to a specific typeDef within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param typeDef typeDef details
     * @throws UserNotAuthorizedException the user is not authorized to retrieve types
     */
    public void  validateUserForTypeRead(String     userId,
                                         String     metadataCollectionName,
                                         TypeDef    typeDef) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForTypeRead";

        if (typeDef != null)
        {
            throwUnauthorizedTypeAccess(userId, typeDef.getGUID(), typeDef.getName(), methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to update a typeDef within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param typeDef typeDef details
     * @throws UserNotAuthorizedException the user is not authorized to maintain types
     */
    public void  validateUserForTypeUpdate(String     userId,
                                           String     metadataCollectionName,
                                           TypeDef    typeDef) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForTypeUpdate";

        if (typeDef != null)
        {
            throwUnauthorizedTypeChange(userId, typeDef.getGUID(), typeDef.getName(), methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to delete a typeDef within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param typeDef typeDef details
     * @throws UserNotAuthorizedException the user is not authorized to maintain types
     */
    public void  validateUserForTypeDelete(String     userId,
                                           String     metadataCollectionName,
                                           TypeDef    typeDef) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForTypeDelete";

        if (typeDef != null)
        {
            throwUnauthorizedTypeChange(userId, typeDef.getGUID(), typeDef.getName(), methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to create a instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForEntityCreate(String       userId,
                                             String       metadataCollectionName,
                                             EntityDetail instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityCreate";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have read access to a specific instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to retrieve instances
     */
    public void  validateUserForEntityRead(String          userId,
                                           String          metadataCollectionName,
                                           EntityDetail    instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityRead";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceAccess(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have read access to a specific instance within a repository.
     *
     * @param userId identifier of user
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to retrieve instances
     */
    public void  validateUserForEntitySummaryRead(String        userId,
                                                  String        metadataCollectionName,
                                                  EntitySummary instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntitySummaryRead";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceAccess(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have read access to a specific instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to retrieve instances
     */
    public void  validateUserForEntityProxyRead(String      userId,
                                                String      metadataCollectionName,
                                                EntityProxy instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityProxyRead";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceAccess(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to update a instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForEntityUpdate(String          userId,
                                             String          metadataCollectionName,
                                             EntityDetail    instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityUpdate";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to update the classification for an entity instance
     * within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @param classification classification details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForEntityClassificationUpdate(String          userId,
                                                           String          metadataCollectionName,
                                                           EntityDetail    instance,
                                                           Classification  classification) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityClassificationUpdate";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to delete a instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForEntityDelete(String       userId,
                                             String       metadataCollectionName,
                                             EntityDetail instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForEntityDelete";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to create a instance within a repository.
     *
     * @param userId identifier of user
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForRelationshipCreate(String       userId,
                                                   String       metadataCollectionName,
                                                   Relationship instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForRelationshipCreate";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have read access to a specific instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to retrieve instances
     */
    public void  validateUserForRelationshipRead(String          userId,
                                                 String          metadataCollectionName,
                                                 Relationship    instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForRelationshipRead";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceAccess(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to update a instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForRelationshipUpdate(String          userId,
                                                   String          metadataCollectionName,
                                                   Relationship    instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForRelationshipUpdate";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Tests for whether a specific user should have the right to delete a instance within a repository.
     *
     * @param userId identifier of user
     * @param metadataCollectionName configurable name of the metadata collection
     * @param instance instance details
     * @throws UserNotAuthorizedException the user is not authorized to maintain instances
     */
    public void  validateUserForRelationshipDelete(String       userId,
                                                   String       metadataCollectionName,
                                                   Relationship instance) throws UserNotAuthorizedException
    {
        final String  methodName = "validateUserForRelationshipDelete";

        if (instance != null)
        {
            String typeName = null;

            if (instance.getType() != null)
            {
                typeName = instance.getType().getTypeDefName();
            }
            throwUnauthorizedInstanceChange(userId, instance.getGUID(), typeName, methodName);
        }
    }


    /**
     * Free up any resources held since the connector is no longer needed.
     *
     * @throws ConnectorCheckedException there is a problem within the connector.
     */
    public  void disconnect() throws ConnectorCheckedException
    {
        super.disconnect();

        logConnectorDisconnecting();
    }
}