import { Routes } from '@angular/router';
import { CadastroComponent } from './cadastro/cadastro.component';
import { LoginComponent } from './login/login.component';
import { TitularesComponent } from './titulares/titulares.component';
import { UsuariosComponent } from './usuarios/usuarios.component';
import { BackupComponent } from './backup/backup.component';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  { path: '', component: CadastroComponent },
  { path: 'login', component: LoginComponent },
  { path: 'titulares', component: TitularesComponent, canActivate: [authGuard] },
  { path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard, adminGuard] },
  { path: 'backup', component: BackupComponent, canActivate: [authGuard, adminGuard] },
  { path: '**', redirectTo: '' },
];
