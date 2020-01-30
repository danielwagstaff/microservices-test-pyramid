package contracts.gameclient.registration

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            name("should register a new player")
            request {
                method POST()
                url '/players'
                body(
                        [
                                "playerName": "nonExistentPlayer"
                        ]
                )
                headers {
                    contentType('application/json')
                }
            }
            response {
                status OK()
                async()
            }
        },
        Contract.make {
            name("should not register already registered player")
            request {
                method 'POST'
                url '/players'
                body(
                        [
                                "playerName": "existentPlayer"
                        ]
                )
                headers {
                    contentType('application/json')
                }
            }
            response {
                status CONFLICT()
                async()
            }
        }
]
