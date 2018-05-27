# money-transfer-rest
Rest service for money transfer(demo project)

## Implemented methods

   "/pending_transactions": {
      "get": {
        "responses": {
          "200": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "array"
                }
              }
            }
          }
        }
      }
    }
    "/transactions/{transactionId}": {
      "get": {
        "parameters": [
          {
            "name": "transactionId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "content": {
              "application/json": {
              }
            }
          }
        }
      }
    },
    "/v1/accounts/{accountId}/transactions": {
      "get": {
        "responses": {
          "200": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "array"
                }
              }
            }
          }
        },
        "parameters": [
          {
            "name": "accountId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ]
      }
    },
    "/v1/accounts": {
      "get": {
        "responses": {
          "200": {
            "content": {
              "application/json": {
                "schema": {
                  "type": "array"
                }
              }
            }
          }
        }
      }
    },
    "/v1/accounts/{accountId}": {
      "get": {
        "responses": {
          "200": {
            "application/json": {
              "schema": {
              }
            }
          }
        },
        "parameters": [
          {
            "name": "accountId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ]
      }
    },
    "/transfer": {
      "post": {
        "responses": {
          "200": {
            "application/json": {
              "schema": {
              }
            }
          }
        },
        "parameters": [
          {
            "name": "fromUserId",
            "in": "query",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "toUserId",
            "in": "query",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "amount",
            "in": "query",
            "required": true,
            "schema": {
              "type": "amount"
            }
          }
        ]
      }
    }
  
