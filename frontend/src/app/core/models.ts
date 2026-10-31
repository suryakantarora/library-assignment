export interface Book {
  title: string;
  available: boolean;
  borrowedByMe: boolean;
  waitlistPosition: number | null;
}

export interface BookDetail {
  title: string;
  available: boolean;
  borrowedByMe: boolean;
  borrowedBy: string | null;
  waitlistSize: number;
  waitlistPosition: number | null;
}

export interface WaitlistPosition {
  title: string;
  position: number;
}

export interface Status {
  borrowed: string[];
  waitlists: WaitlistPosition[];
}

export interface LoginResponse {
  token: string;
  name: string;
  admin: boolean;
  notifications: string[];
  status: Status;
}

export interface Session {
  token: string;
  name: string;
  admin: boolean;
}

export interface ActionResponse {
  message: string;
}

export interface Feedback {
  text: string;
  kind: 'success' | 'error';
}
