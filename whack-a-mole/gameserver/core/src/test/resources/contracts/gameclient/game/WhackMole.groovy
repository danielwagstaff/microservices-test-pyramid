package contracts.gameclient.game

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            name("should whack mole")
            request {
                method POST()
                url '/game/moles'
                body(
                        [
                                "playerNameDto": ["playerName": "existentPlayer"],
                                "moleIdDto"    : ["moleId": "11111111-1111-1111-1111-111111111111"]
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
            name("should not whack non-existent mole")
            request {
                method POST()
                url '/game/moles'
                body(
                        [
                                "playerNameDto": ["playerName": "existentPlayer"],
                                "moleIdDto"    : ["moleId": '00000000-0000-0000-0000-000000000000']
                        ]
                )
                headers {
                    contentType('application/json')
                }
            }
            response {
                status NOT_FOUND()
                async()
            }
        },
        Contract.make {
            name("non-player should not whack mole")
            request {
                method POST()
                url '/game/moles'
                body(
                        [
                                "playerNameDto": ["playerName": "nonExistentPlayer"],
                                "moleIdDto"    : ["moleId": "11111111-1111-1111-1111-111111111111"]
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
