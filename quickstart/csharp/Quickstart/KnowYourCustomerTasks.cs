using LittleHorse.Sdk.Worker;

namespace Quickstart
{
    public class KnowYourCustomerTasks
    {
        private static readonly Random random = new Random();

        [LHTaskMethod("verify-identity")]
        public string VerifyIdentity(string firstName, string lastName, int ssn)
        {
            if (random.NextDouble() < 0.25)
            {
                throw new Exception("The external identity verification API is down");
            }
            return "Successfully called external API to request verification for " + firstName + " " + lastName;
        }

        [LHTaskMethod("notify-customer-not-verified")]
        public string NotifyCustomerNotVerified(string firstName, string lastName)
        {
            return "Notification sent to customer " + firstName + " " + lastName
                + " that their identity has not been verified";
        }

        [LHTaskMethod("notify-customer-verified")]
        public string NotifyCustomerVerified(string firstName, string lastName)
        {
            return "Notification sent to customer " + firstName + " " + lastName
                + " that their identity has been verified";
        }
    }
}
