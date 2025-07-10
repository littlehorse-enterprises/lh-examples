package main

import (
	"context"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
	"log"
)

func main() {
	config := common.NewConfigFromEnv()
	client, err := config.GetGrpcClient()

	if err != nil {
		log.Fatal(err)
	}

	(*client).PutExternalEventDef(context.Background(),
		&model.PutExternalEventDefRequest{
			Name: "passport-submitted",
		})

	(*client).PutUserTaskDef(context.Background(), &model.PutUserTaskDefRequest{
		Name: "manual-verification",
		Fields: []*model.UserTaskField{
			{
				Name: "approved",
				Type: model.VariableType_BOOL,
			},
		},
	})

	wf := wflib.NewWorkflow(KycWorkflow, "kyc")
	putWf, err := wf.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := (*client).PutWfSpec(context.Background(), putWf)
	if err != nil {
		log.Fatal(err)
	}

	common.PrintProto(resp)
}
