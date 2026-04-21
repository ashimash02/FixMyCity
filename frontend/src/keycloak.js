import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'civic-tracker',
  clientId: 'civic-tracker-app',
})

export default keycloak
