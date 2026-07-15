prefix    = "pagopa"
env       = "dev"
env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "pagoPA"
  Source      = "https://github.com/pagopa/pagopa-cruscotto-sert-backend"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

apim_dns_zone_prefix = "dev.platform"
external_domain      = "pagopa.it"
hostname             = "crusc8.itn.internal.dev.platform.pagopa.it"
origin               = "crusc8.dev.platform.pagopa.it"
