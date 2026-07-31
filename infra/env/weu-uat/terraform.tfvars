prefix    = "pagopa"
env       = "uat"
env_short = "u"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Uat"
  Owner       = "pagoPA"
  Source      = "https://github.com/pagopa/pagopa-cruscotto-sert-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

apim_dns_zone_prefix = "uat.platform"
external_domain      = "pagopa.it"
hostname             = "crusc8.itn.internal.uat.platform.pagopa.it"
origin               = "crusc8.uat.platform.pagopa.it"
