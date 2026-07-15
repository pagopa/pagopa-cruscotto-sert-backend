locals {
  repo_name = "pagopa-cruscotto-sert-backend"

  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname

  # API con Subscription Key
  sert_subkey = {
    display_name = "Cruscotto Sert pagoPA backend service API (Subscription Key)"
    description  = "Cruscotto Sert pagoPA backend service API (Subscription Key)"
    path         = "smo/cruscotto-sert-subkey"
  }

  # API senza Subscription Key
  sert = {
    display_name = "Cruscotto Sert pagoPA backend service API"
    description  = "Cruscotto Sert pagoPA backend service API"
    path         = "smo/cruscotto-sert"
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
# 1. SERT API (Subscription Key)
# ----------------------------------------------------
resource "azurerm_api_management_api_version_set" "api_version_set_sert_subkey" {
  name                = format("%s-${local.repo_name}-sert-subkey", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.sert_subkey.display_name
  versioning_scheme   = "Segment"
}

resource "time_sleep" "wait_after_sert_subkey_vs" {
  depends_on = [
    azurerm_api_management_api_version_set.api_version_set_sert_subkey
  ]

  create_duration = "120s"
}

module "api_sert_subkey_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                  = format("%s-${local.repo_name}-sert-subkey", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id_subkey]

  subscription_required = true // Subscription key required

  version_set_id = azurerm_api_management_api_version_set.api_version_set_sert_subkey.id
  api_version    = "v1"

  description  = local.sert_subkey.description
  display_name = local.sert_subkey.display_name
  path         = local.sert_subkey.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value = templatefile("../openapi/openapi_sert_subkey.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname   = var.hostname
    origin     = var.origin
  })

  depends_on = [
    azurerm_api_management_api_version_set.api_version_set_sert_subkey,
    time_sleep.wait_after_sert_subkey_vs
  ]
}

# ----------------------------------------------------
# 2. SERT API (No Subscription Key)
# ----------------------------------------------------
resource "azurerm_api_management_api_version_set" "api_version_set_sert" {
  name                = format("%s-${local.repo_name}", var.env_short)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.sert.display_name
  versioning_scheme   = "Segment"
}

module "api_sert_v1" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                  = format("%s-${local.repo_name}", var.env_short)
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.product_id]

  subscription_required = false // No subscription key required JWT validation

  version_set_id = azurerm_api_management_api_version_set.api_version_set_sert.id
  api_version    = "v1"

  description  = local.sert.description
  display_name = local.sert.display_name
  path         = local.sert.path
  protocols    = ["https"]

  service_url = null

  content_format = "openapi"
  content_value = templatefile("../openapi/openapi_sert.json", {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy_jwt.xml", {
    hostname   = var.hostname
    origin     = var.origin
  })
}
