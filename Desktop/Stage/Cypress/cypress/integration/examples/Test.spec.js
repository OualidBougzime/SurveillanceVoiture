
describe('login test ', function() {
it('Example',function(){
cy.visit('https://leonar.interleo.eu/')
cy.get('.buttons > .btn').click()
cy.get('#forminator-field-text-1').type('oualid.bougzime@utbm.fr')
cy.get('#forminator-field-password-1').type('Maroc@20141999')
cy.get('.forminator-checkbox > [aria-hidden="true"]').click()
cy.get('.forminator-button').click()
cy.get('#forminator-field-post-title-postdata-1').type('APP mobiles de détection de machines ou posters')
cy.get('#forminator-wp-editor-forminator-field-post-content-postdata-1-60d32fbd7365f_ifr').type('Poster de l’application mobile décrite sur ce site web.')
;
})
}) 