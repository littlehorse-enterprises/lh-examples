import { ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { VariableValue } from "littlehorse-client/dist/proto";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

// TODO: properly document this function
// Helper function to create typed variable values
export const createVariableValue = (
  type: 'str' | 'bool' | 'int' | 'double' | 'jsonObj' | 'jsonArr',
  value: string | boolean | number
): VariableValue => {
  switch (type) {
    case 'str':
      return { str: String(value) };
    case 'bool':
      return { bool: Boolean(value) };
    case 'int':
      return { int: Number(value) };
    case 'double':
      return { double: Number(value) };
    case 'jsonObj':
      return { jsonObj: JSON.stringify(value) };
    case 'jsonArr':
      return { jsonArr: JSON.stringify(value) };
    default:
      throw new Error(`Unknown variable type: ${type}`);
  }
};