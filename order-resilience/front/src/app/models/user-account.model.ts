export interface UserAccount {
  id: number;
  name: string;
  email: string;
  description?: string;
  type?: 'CUSTOMER' | 'EMPLOYEE';
  profileImage?: string;
}
