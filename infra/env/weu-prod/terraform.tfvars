prefix    = "pagopa"
env       = "prod"
env_short = "p"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "pagoPA"
  Source      = "https://github.com/pagopa/pagopa-cruscotto-sert-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

apim_dns_zone_prefix = "platform"
external_domain      = "pagopa.it"
hostname             = "crusc8.itn.internal.platform.pagopa.it"
origin               = "platform.pagopa.it"
crusc8_aud           = "TODO-replace-with-app-client-id"
