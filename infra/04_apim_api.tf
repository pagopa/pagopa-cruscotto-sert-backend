module "api_sert" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//api_management_api?ref=v8.62.1"

  name = format("%s-%s-sert", var.env_short, local.repo_name)

  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.sert.display_name
  description  = local.apis.sert.description

  path = local.apis.sert.path

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

  name = format("%s-%s-management", var.env_short, local.repo_name)

  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.management.display_name
  description  = local.apis.management.description

  path = local.apis.management.path

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

  name = format("%s-%s-auth", var.env_short, local.repo_name)

  api_management_name = local.apim.name
  resource_group_name = local.apim.rg

  product_ids           = [local.apim.product_id]
  subscription_required = false

  version_set_id = azurerm_api_management_api_version_set.api_version_set.id
  api_version    = "v1"

  display_name = local.apis.auth.display_name
  description  = local.apis.auth.description

  path = local.apis.auth.path

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
