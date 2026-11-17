# pagopa-cruscotto-sert-backend
Cruscotto SErvizio Ricerca Rransazioni (aka sert)

## FDR API mocks

The FDR endpoints return contract-shaped sample responses without reading or writing FDR data.
JWT search endpoints are available under `/api/fdr` and use endpoint-specific `GTW.FDR_*`
authorities; subscription-key search endpoints are available under `/sub/api/fdr` without
`@PreAuthorize`. Administrative endpoints are JWT-only under `/api/admin/fdr`. Reingestion
requests are kept in memory for the lifetime of the application and are not forwarded to
`cruscotto-ingestor`. The JWT endpoint `/api/fdr/flow-revisions/{revisionId}/export` streams
the mock revision data as CSV; occurrence searches include revision metadata and support the
optional `flowId` filter for IUV searches.
