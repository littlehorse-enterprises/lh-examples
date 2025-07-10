
import { getSku } from "./getSku";
import { Form } from "./form";
import { Container } from "./container";
import { Notifications } from "./notifications";
import { Metadata } from "next";
type ItemParams = {
  params: {
    sku: string
  }
}
export const dynamic = "force-dynamic";

export const generateMetadata = ({ params }: {params: {sku: string}}): Metadata => {
  return {
    title: `Product - ${params.sku}`,
  };
};

export default async function Item({ params: { sku } }: ItemParams) {
  const { stock, price } = await getSku(sku);

  return (
    <Container>
      <Form stock={stock} price={price}/>
      <Notifications/>
    </Container>
  );
}
