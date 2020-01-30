package contracts.gameclient.registration

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            name("should remove an existing player")
            request {
                method DELETE()
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
                status OK()
                async()
            }
        },
        Contract.make {
            name("should not remove a non-existent player")
            request {
                method DELETE()
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
                status BAD_REQUEST()
                async()
            }
        }
]
