import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AutenticacaoService } from '../../services/autenticacao.service';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [ReactiveFormsModule, CommonModule],
})
export class LoginComponent {
  loginForm: FormGroup;
  loginError: string | null = null;
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private autService: AutenticacaoService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      email: [null, [Validators.required, Validators.email]],
      senha: [null, [Validators.required, Validators.minLength(6)]],
    });
  }

  get f() { return this.loginForm.controls; }

  login() {
    if (this.loginForm.invalid) {
      this.loginError = 'Por favor, preencha os campos corretamente.';
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loginError = null;
    this.loading = true;

    const { email, senha } = this.loginForm.value;
    this.autService.autenticar(email, senha).subscribe({
      next: (resp) => {
        // resp: { tokenType, accessToken, expiresIn, user{ id, nome, email, roles } }
        // Guarde o access token em memória (ex.: AuthService) — interceptor usa daí
        this.autService.setSession(resp); // você implementará
        this.router.navigate(['/']);
      },
      error: () => this.loginError = 'E-mail ou senha inválidos.',
      complete: () => this.loading = false
    });
  }
}
