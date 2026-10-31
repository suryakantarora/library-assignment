import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { LoginPage } from './login-page';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let element: HTMLElement;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(LoginPage);
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  async function submitWith(name: string): Promise<void> {
    const input = element.querySelector('input') as HTMLInputElement;
    input.value = name;
    input.dispatchEvent(new Event('input'));
    (element.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('asks for a name when the field is blank', async () => {
    const login = vi.spyOn(TestBed.inject(AuthService), 'login');

    await submitWith('   ');

    expect(login).not.toHaveBeenCalled();
    expect(element.querySelector('.error')?.textContent).toContain('Please enter your name.');
  });

  it('logs in and moves to the library', async () => {
    const login = vi.spyOn(TestBed.inject(AuthService), 'login').mockResolvedValue();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    await submitWith(' Alice ');

    expect(login).toHaveBeenCalledWith('Alice');
    expect(navigate).toHaveBeenCalledWith(['/']);
  });
});
