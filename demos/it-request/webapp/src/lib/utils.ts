import { ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { VariableType, VariableValue } from "littlehorse-client/proto";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

/**
 * VariableValue is a structure containing a value in LittleHorse. It can be
 * used to pass input variables into a WfRun/ThreadRun/TaskRun/etc, as output
 * from a TaskRun, as the value of a WfRun's Variable, etc.
 */
export const createVariableValue = (
  type: keyof typeof VariableType,
  value: string | boolean | number
): VariableValue => {
  switch (type) {
    case 'STR':
      return { str: String(value) };
    case 'BOOL':
      return { bool: Boolean(value) };
    case 'INT':
      return { int: Number(value) };
    case 'DOUBLE':
      return { double: Number(value) };
    case 'JSON_OBJ':
      return { jsonObj: JSON.stringify(value) };
    case 'JSON_ARR':
      return { jsonArr: JSON.stringify(value) };
    default:
      throw new Error(`Unknown variable type: ${type}`);
  }
};