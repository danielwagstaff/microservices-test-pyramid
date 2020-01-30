package contracts.gameclient.game

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            name("should get all active moles")
            request {
                method GET()
                url '/game/moles'
            }
            response {
                status OK()
                async()
                body(
                        [
                                ["moleId": $(regex(uuid()))],
                                ["moleId": $(regex(uuid()))]
                        ]
                )
                headers {
                    contentType('application/json')
                }
            }
        }
]
