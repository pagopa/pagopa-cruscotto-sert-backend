locals {
  repo_name = "pagopa-cruscotto-sert-backend"

  host     = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname = var.hostname

  apis = {
    sert = {
      display_name = "Cruscotto Sert pagoPA backend service API"
      description  = "Cruscotto Sert backend service API"
      path         = "smo/cruscotto-sert"
      openapi      = "../openapi/openapi_sert.json"
    }

    management = {
      display_name = "Cruscotto Management pagoPA backend service API"
      description  = "Cruscotto Management backend service API"
      path         = "smo/cruscotto-management"
      openapi      = "../openapi/openapi_management.json"
    }

    auth = {
      display_name = "Cruscotto AUTH pagoPA backend service API"
      description  = "Cruscotto AUTH backend service API"
      path         = "smo/cruscotto-auth"
      openapi      = "../openapi/openapi_auth.json"
    }
  }
}

# =========================
# API GROUP
# =========================
resource "azurerm_api_management_group" "api_group" {
  name                = local.apim.product_id
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name

  display_name = "Cruscotto Sert APIs"
  description  = "Cruscotto Sert APIs"
}

# =========================
# VERSION SET
# =========================
resource "azurerm_api_management_api_version_set" "api_version_set" {
  name                = format("%s-%s", var.env_short, local.repo_name)
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name

  display_name      = "Cruscotto Sert pagoPA backend service API"
  versioning_scheme = "Segment"
}

# =========================
# APIs
# =========================
module "apis" {
  for_each = local.apis

  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name = format(
    "%s-%s-%s",
    var.env_short,
    local.repo_name,
    each.key
  )

  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = each.value.display_name
  description  = each.value.description

  path      = each.value.path
  protocols = ["https"]

  service_url = null

  content_format = "openapi"

  content_value = templatefile(each.value.openapi, {
    host = local.host
  })

  xml_content = templatefile("./policy/_base_policy.xml", {
    hostname = local.hostname
  })
}
