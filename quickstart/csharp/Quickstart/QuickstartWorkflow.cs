using LittleHorse.Sdk.Workflow.Spec;
using LittleHorse.Sdk.Common.Proto;

namespace Quickstart
{
	public static class QuickstartWorkflow
	{
		public const string WorkflowName = "quickstart";
		public const string IdentityVerifiedEvent = "identity-verified";
		public const string VerifyIdentityTask = "verify-identity";
		public const string NotifyCustomerVerifiedTask = "notify-customer-verified";
		public const string NotifyCustomerNotVerifiedTask = "notify-customer-not-verified";

		public static Workflow GetWorkflow()
		{
			void MyEntryPoint(WorkflowThread wf)
			{
				var firstName = wf.DeclareStr("first-name").Searchable().Required();
				var lastName = wf.DeclareStr("last-name").Searchable().Required();
				var ssn = wf.DeclareInt("ssn").Masked().Required();

				var identityVerified = wf.DeclareBool("identity-verified").Searchable();

				wf.Execute(VerifyIdentityTask, firstName, lastName, ssn).WithRetries(3);

				var identityVerificationResult = wf.WaitForEvent(IdentityVerifiedEvent).WithTimeout(60 * 60 * 24 * 3);

				wf.HandleError(
					identityVerificationResult,
					LHErrorType.Timeout,
					handler =>
					{
						handler.Execute(NotifyCustomerNotVerifiedTask, firstName, lastName);
						handler.Fail("customer-not-verified", "Unable to verify customer identity in time.");
					}
				);

				identityVerified.Assign(identityVerificationResult);

				wf.DoIf(
					wf.Condition(identityVerified, Comparator.Equals, true),
					ifThread => ifThread.Execute(NotifyCustomerVerifiedTask, firstName, lastName),
					elseThread => elseThread.Execute(NotifyCustomerNotVerifiedTask, firstName, lastName)
				);
			}

			return new Workflow(WorkflowName, MyEntryPoint);
		}
	}
}
