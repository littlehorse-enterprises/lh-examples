using LittleHorse.Sdk.Worker;

namespace Quickstart
{
    public class KnowYourCustomerTasks
    {
        private static readonly Random random = new Random();

        [LHTaskMethod("verify-identity")]
        public async Task<string> VerifyIdentity(string fullName, string email, int ssn)
        {
            if (random.NextDouble() < 0.25)
            {
                throw new Exception("The external identity verification API is down");
            }
            return "Successfully called external API to request verification for " + fullName + " at " + email;
        }

        [LHTaskMethod("notify-customer-not-verified")]
        public async Task<string> NotifyCustomerNotVerified(string fullName, string email)
        {
            return "Notification sent to customer " + fullName + " at " + email
                + " that their identity has not been verified";
        }

        [LHTaskMethod("notify-customer-verified")]
        public async Task<string> NotifyCustomerVerified(string fullName, string email)
        {
            return "Notification sent to customer " + fullName + " at " + email
                + " that their identity has been verified";
        }
    }
}
