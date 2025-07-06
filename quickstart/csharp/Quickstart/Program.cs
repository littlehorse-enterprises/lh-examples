using Quickstart;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;

public abstract class Program
{
    private static readonly KnowYourCustomerTasks tasks = new KnowYourCustomerTasks();
    private static readonly LHConfig config = new LHConfig();

    private static async Task RegisterMetadata()
    {
        var client = config.GetGrpcClientInstance();

        Console.WriteLine("Registering ExternalEventDef");
        client.PutExternalEventDef(new PutExternalEventDefRequest
        {
            Name = QuickstartWorkflow.IdentityVerifiedEvent,
            CorrelatedEventConfig = new CorrelatedEventConfig{},
        });

        Console.WriteLine("Registering TaskDefs");
        var verifyIdentityWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.VerifyIdentityTask, config);
        var notifyCustomerVerifiedWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerVerifiedTask, config);
        var notifyCustomerNotVerifiedWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerNotVerifiedTask, config);

        await verifyIdentityWorker.RegisterTaskDef();
        await notifyCustomerVerifiedWorker.RegisterTaskDef();
        await notifyCustomerNotVerifiedWorker.RegisterTaskDef();

        Console.WriteLine("Registering WfSpec");
        var workflow = QuickstartWorkflow.GetWorkflow();
        await workflow.RegisterWfSpec(client);
    }

    private static async Task StartTaskWorkers()
    {
        var verifyIdentityWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.VerifyIdentityTask, config);
        var notifyCustomerVerifiedWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerVerifiedTask, config);
        var notifyCustomerNotVerifiedWorker = new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerNotVerifiedTask, config);

        Console.WriteLine("Starting workers");
        verifyIdentityWorker.Start();
        notifyCustomerVerifiedWorker.Start();
        notifyCustomerNotVerifiedWorker.Start();

        Console.WriteLine("Workers started");
        Console.WriteLine("Press enter to stop the workers");
        Console.ReadLine();

        Console.WriteLine("Stopping workers");
        verifyIdentityWorker.Close();
        notifyCustomerVerifiedWorker.Close();
        notifyCustomerNotVerifiedWorker.Close();
    }

    static async Task Main(string[] args)
    {
        if (args.Length != 1 || (args[0] != "register" && args[0] != "workers"))
        {
            Console.Error.WriteLine("Please provide one argument: either 'register' or 'workers'");
            Environment.Exit(1);
        }

        if (args[0] == "register")
        {
            await RegisterMetadata();
        }
        else
        {
            await StartTaskWorkers();
        }
    }
}
