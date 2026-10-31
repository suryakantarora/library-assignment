import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoginResponse } from './models';

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  const response: LoginResponse = {
    token: 'abc',
    name: 'admin',
    admin: true,
    notifications: ['You got the book "1984" from your waitlist'],
    status: { borrowed: [], waitlists: [] },
  };

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('starts logged out', () => {
    expect(auth.isLoggedIn()).toBe(false);
    expect(auth.token()).toBeNull();
  });

  it('stores the session and builds welcome lines on login', async () => {
    const pending = auth.login('admin');
    http.expectOne('/api/auth/login').flush(response);
    await pending;

    expect(auth.token()).toBe('abc');
    expect(auth.welcome()).toEqual([
      'Hello, admin!',
      'You have access to library management.',
      'You got the book "1984" from your waitlist',
    ]);
    expect(sessionStorage.getItem('library.session')).toContain('abc');
  });

  it('clears the session and goes to login on logout', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const login = auth.login('admin');
    http.expectOne('/api/auth/login').flush(response);
    await login;

    const logout = auth.logout();
    http.expectOne('/api/auth/logout').flush({ message: 'Goodbye, admin!' });
    await logout;

    expect(auth.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('library.session')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
    expect(auth.farewell()).toBe('Goodbye, admin!');
  });

  it('summarises borrowed books and wait lists for a returning member', async () => {
    const pending = auth.login('Bob');
    http.expectOne('/api/auth/login').flush({
      ...response,
      name: 'Bob',
      admin: false,
      notifications: [],
      status: { borrowed: ['The Great Gatsby', '1984'], waitlists: [{ title: 'Dune', position: 2 }] },
    });
    await pending;

    expect(auth.welcome()).toEqual([
      'Hello, Bob!',
      'Your borrowed books:',
      '1. The Great Gatsby',
      '2. 1984',
      'Your wait lists:',
      '1. Dune - position 2',
    ]);
  });

  it("tells a member without books that nothing is borrowed", async () => {
    const pending = auth.login('Carol');
    http.expectOne('/api/auth/login').flush({ ...response, name: 'Carol', admin: false, notifications: [] });
    await pending;

    expect(auth.welcome()).toEqual(['Hello, Carol!', "You don't have any books borrowed yet."]);
  });
});
