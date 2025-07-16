# How to deploy to BCGov API Gateway (Kong)

Developer documentation: https://developer.gov.bc.ca/docs/default/component/aps-infra-platform-docs/
Gateway homepage: https://api.gov.bc.ca/manager/gateways/list

Requirements: 
* Download the GWA CLI and follow instructions to setup here https://developer.gov.bc.ca/docs/default/component/aps-infra-platform-docs/tutorials/quick-start/

Instructions: 
* Login to gwa ```gwa login```
* Set your current gateway to the correct gateway id emer-report-api ```gwa config set --gateway emer-report-api```
* apply the config to the environment ```gwa apply -i gw-config.yaml```

Tips: 
* Remember to add the network policies to the environments to allow the API gateway to route to our namespace https://developer.gov.bc.ca/docs/default/component/aps-infra-platform-docs/how-to/upstream-services/

