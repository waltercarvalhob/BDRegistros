import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

function somenteDigitos(valor: string): string {
  return (valor || '').replace(/\D/g, '');
}

function digitoValido(cpf: string, tamanho: number): boolean {
  let soma = 0;
  for (let i = 0; i < tamanho; i++) {
    soma += Number(cpf[i]) * (tamanho + 1 - i);
  }
  const resto = soma % 11;
  const esperado = resto < 2 ? 0 : 11 - resto;
  return esperado === Number(cpf[tamanho]);
}

export function cpfValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const cpf = somenteDigitos(control.value);
    if (!cpf) {
      return null;
    }
    const digitosIguais = new Set(cpf.split('')).size === 1;
    const valido = cpf.length === 11 && !digitosIguais && digitoValido(cpf, 9) && digitoValido(cpf, 10);
    return valido ? null : { cpfInvalido: true };
  };
}
