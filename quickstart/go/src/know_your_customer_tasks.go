package main

import (
	"errors"
	"math/rand/v2"
)

func VerifyIdentity(firstName, lastName string, ssn int) (string, error) {
	if rand.Float32() < 0.25 {
		return "", errors.New("the external identity verification API is down")
	}
	return "Successfully called external API to request verification for " + firstName + " " + lastName, nil
}

func NotifyCustomerVerified(firstName, lastName string) string {
	return "Notification sent to customer " + firstName + " " + lastName + " that their identity has been verified"
}

func NotifyCustomerNotVerified(firstName, lastName string) string {
	return "Notification sent to customer " + firstName + " " + lastName + " that their identity has not been verified"
}
