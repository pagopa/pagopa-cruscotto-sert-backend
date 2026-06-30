locals {
  repo_name = "pagopa-cruscotto-sert-backend"

  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname

  apis = {
    sert = {
      display_name = "Cruscotto Sert pagoPA backend service API"
      description  = "Cruscotto Sert pagoPA backend service API"
      path         = "smo/cruscotto-sert"
      openapi      = "../openapi/openapi_sert.json"
    }

    management = {
      display_name = "Cruscotto Management pagoPA backend service API"
      description  = "Cruscotto Management pagoPA backend service API"
      path         = "smo/cruscotto-management"
      openapi      = "../openapi/openapi_management.json"
    }

    auth = {
      display_name = "Cruscotto AUTH pagoPA backend service API"
      description  = "Cruscotto AUTH pagoPA backend service API"
      path         = "smo/cruscotto-auth"
      openapi      = "../openapi/openapi_auth.json"
    }
  }
}

resource "azurerm_api_management_group" "api_group" {
  name                = local.apim.product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name

  display_name = "Cruscotto Sert APIs"
  description  = "Group for Cruscotto Sert APIs"
}

resource "azurerm_api_management_api_version_set" "api_version_set" {
  name                = format("%s-%s", var.env_short, local.repo_name)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name

  display_name      = "Cruscotto Sert pagoPA backend service API"
  versioning_scheme = "Segment"
}

module "api_sert" {
  depends_on = [azurerm_api_management_api_version_set.api_version_set]

  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                = format("%s-%s-sert", var.env_short, local.repo_name)
  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.sert.display_name
  description  = local.apis.sert.description

  path      = local.apis.sert.path
  protocols = ["https"]

  service_url = null

  content_format = "openapi"

  content_value = templatefile(local.apis.sert.openapi, {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = local.hostname
  })
}

module "api_management" {
  depends_on = [module.api_sert]

  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                = format("%s-%s-management", var.env_short, local.repo_name)
  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.management.display_name
  description  = local.apis.management.description

  path      = local.apis.management.path
  protocols = ["https"]

  service_url = null

  content_format = "openapi"

  content_value = templatefile(local.apis.management.openapi, {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = local.hostname
  })
}

module "api_auth" {
  depends_on = [module.api_management]

  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name                = format("%s-%s-auth", var.env_short, local.repo_name)
  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.auth.display_name
  description  = local.apis.auth.description

  path      = local.apis.auth.path
  protocols = ["https"]

  service_url = null

  content_format = "openapi"

  content_value = templatefile(local.apis.auth.openapi, {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = local.hostname
  })
}
