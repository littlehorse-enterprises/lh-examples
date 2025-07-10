import Link from "next/link";
import { getSkus } from "./getSkus";

export const dynamic = "force-dynamic";

export default async function Index() {
  const skus = await getSkus();

  return (
    <div className="flex font-sans bg-white shadow-md rounded">
      <div className="flex-auto w-96 p-6">
        <h1 className="text-lg font-bold mb-4">Available Products</h1>
        {skus.map((sku, i) => (
          <Link key={i} href={`/${sku}`}>
            <div className="text-md hover:bg-cyan-50 mb-2 text-cyan-600 border font-medium rounded p-2 text-center capitalize">
              {sku}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
