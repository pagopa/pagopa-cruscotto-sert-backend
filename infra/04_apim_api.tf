locals {
  repo_name = "pagopa-cruscotto-sert-backend"

  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname

  # Configurazioni per ciascuna delle 3 API
  auth = {
    display_name = "Cruscotto Sert Auth pagoPA backend service API"
    description  = "Cruscotto Sert Auth pagoPA backend service API"
    path         = "smo/cruscotto-sert-auth"
  }

  management = {
    display_name = "Cruscotto Sert Management pagoPA backend service API"
    description  = "Cruscotto Sert Management pagoPA backend service API"
    path         = "smo/cruscotto-sert-management"
  }

  sert = {
    display_name = "Cruscotto Sert pagoPA backend service API"
    description  = "Cruscotto Sert pagoPA backend service API"
    path         = "smo/cruscotto-sert-search"
  }
}

resource "azurerm_api_management_group" "api_group" {
  name                = local.apim.product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.sert.display_name
  description         = local.sert.description
}

# ----------------------------------------------------
# 1. AUTH API
# ----------------------------------------------------
resource "azurerm_api_management_api_version_set" "api_version_set_auth" {
  name                = format("%s-${local.repo_name}-auth", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.auth.display_name
  versioning_scheme   = "Segment"
}

module "api_auth_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                  = format("%s-${local.repo_name}-auth", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set_auth.id
  api_version    = "v1"

  description  = local.auth.description
  display_name = local.auth.display_name
  path         = local.auth.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi_auth.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
  depends_on = [
    azurerm_api_management_api_version_set.api_version_set_auth,
    time_sleep.wait_after_auth_vs
  ]
}


# ----------------------------------------------------
# 2. MANAGEMENT API
# ----------------------------------------------------
resource "azurerm_api_management_api_version_set" "api_version_set_management" {
  name                = format("%s-${local.repo_name}-management", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.management.display_name
  versioning_scheme   = "Segment"
}

module "api_management_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                  = format("%s-${local.repo_name}-management", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set_management.id
  api_version    = "v1"

  description  = local.management.description
  display_name = local.management.display_name
  path         = local.management.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi_management.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
  depends_on = [
    azurerm_api_management_api_version_set.api_version_set_management,
    time_sleep.wait_after_management_vs
  ]
}

# ----------------------------------------------------
# 3. SERT API
# ----------------------------------------------------
resource "azurerm_api_management_api_version_set" "api_version_set" {
  name                = format("%s-${local.repo_name}", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.sert.display_name
  versioning_scheme   = "Segment"
}

module "api_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                  = format("%s-${local.repo_name}", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  description  = local.sert.description
  display_name = local.sert.display_name
  path         = local.sert.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value  = templatefile("../openapi/openapi_sert.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = var.hostname
  })
}

# Helper sleep resources – give Azure time after each version‑set creation before the API module runs.
resource "time_sleep" "wait_after_auth_vs" {
  depends_on = [azurerm_api_management_api_version_set.api_version_set_auth]
  create_duration = "120s"
}

resource "time_sleep" "wait_after_management_vs" {
  depends_on = [azurerm_api_management_api_version_set.api_version_set_management]
  create_duration = "60s"
}
