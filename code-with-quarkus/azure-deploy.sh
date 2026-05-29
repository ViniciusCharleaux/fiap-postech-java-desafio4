#!/bin/bash

# ============================================================
# Script de deploy do TC4 Feedback Platform para Azure
# Execute: bash azure-deploy.sh
# ============================================================

set -e

# ---- Variáveis -- ajuste conforme necessário ---------------
RESOURCE_GROUP="rg-tc4-feedback"
LOCATION="brazilsouth"
POSTGRES_SERVER="tc4-postgres-server"
POSTGRES_DB="feedback_db"
POSTGRES_USER="tc4_user"
POSTGRES_PASSWORD="TC4@Password2024"
STORAGE_ACCOUNT="tc4feedbackstorage"
FUNCTION_APP="tc4-feedback-functions"
COMMUNICATION_SERVICE="tc4-communication"
INSIGHTS_NAME="tc4-feedback-insights"
ADMIN_EMAIL="seu-email-admin@gmail.com"
# ------------------------------------------------------------

echo ""
echo "========================================"
echo " TC4 Feedback - Deploy Azure"
echo "========================================"
echo ""

# 1. Login (abre browser)
echo "[1/10] Verificando login no Azure..."
az account show > /dev/null 2>&1 || az login

# 2. Resource Group
echo "[2/10] Criando Resource Group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION \
  --output none

# 3. PostgreSQL
echo "[3/10] Criando PostgreSQL Flexible Server..."
az postgres flexible-server create \
  --resource-group $RESOURCE_GROUP \
  --name $POSTGRES_SERVER \
  --location $LOCATION \
  --admin-user $POSTGRES_USER \
  --admin-password "$POSTGRES_PASSWORD" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 16 \
  --yes \
  --output none

echo "       Liberando acesso para Azure Services..."
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --name $POSTGRES_SERVER \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0 \
  --output none

echo "       Criando banco de dados..."
az postgres flexible-server db create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --database-name $POSTGRES_DB \
  --output none

# 4. Storage Account
echo "[4/10] Criando Storage Account..."
az storage account create \
  --name $STORAGE_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard_LRS \
  --output none

# 5. Function App
echo "[5/10] Criando Function App..."
az functionapp create \
  --resource-group $RESOURCE_GROUP \
  --consumption-plan-location $LOCATION \
  --runtime java \
  --runtime-version 21 \
  --functions-version 4 \
  --name $FUNCTION_APP \
  --storage-account $STORAGE_ACCOUNT \
  --os-type linux \
  --output none

# 6. Application Insights
echo "[6/10] Criando Application Insights..."
az monitor app-insights component create \
  --app $INSIGHTS_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --output none

INSIGHTS_KEY=$(az monitor app-insights component show \
  --app $INSIGHTS_NAME \
  --resource-group $RESOURCE_GROUP \
  --query connectionString -o tsv)

# 7. Azure Communication Services
echo "[7/10] Criando Azure Communication Services..."
az communication create \
  --name $COMMUNICATION_SERVICE \
  --resource-group $RESOURCE_GROUP \
  --location global \
  --data-location unitedstates \
  --output none

COMM_CONNECTION_STRING=$(az communication list-key \
  --name $COMMUNICATION_SERVICE \
  --resource-group $RESOURCE_GROUP \
  --query primaryConnectionString -o tsv)

# 8. Variáveis de ambiente na Function App
echo "[8/10] Configurando variáveis de ambiente..."
JDBC_URL="jdbc:postgresql://${POSTGRES_SERVER}.postgres.database.azure.com:5432/${POSTGRES_DB}?sslmode=require"

az functionapp config appsettings set \
  --name $FUNCTION_APP \
  --resource-group $RESOURCE_GROUP \
  --settings \
    "QUARKUS_DATASOURCE_JDBC_URL=${JDBC_URL}" \
    "QUARKUS_DATASOURCE_USERNAME=${POSTGRES_USER}" \
    "QUARKUS_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}" \
    "AZURE_COMMUNICATION_CONNECTION_STRING=${COMM_CONNECTION_STRING}" \
    "APPLICATIONINSIGHTS_CONNECTION_STRING=${INSIGHTS_KEY}" \
    "FEEDBACK_ADMIN_EMAIL=${ADMIN_EMAIL}" \
    "QUARKUS_PROFILE=prod" \
  --output none

# 9. Build do projeto
echo "[9/10] Fazendo build do projeto..."
./mvnw clean package -DskipTests -Dquarkus.profile=prod

# 10. Deploy
echo "[10/10] Fazendo deploy para Azure Functions..."
./mvnw azure-functions:deploy

echo ""
echo "========================================"
echo " Deploy concluído com sucesso!"
echo "========================================"
echo ""
echo "URL da aplicação:"
az functionapp show \
  --name $FUNCTION_APP \
  --resource-group $RESOURCE_GROUP \
  --query defaultHostName -o tsv | xargs -I{} echo " https://{}/api"
echo ""
echo "Application Insights:"
echo " https://portal.azure.com -> $INSIGHTS_NAME"
echo ""