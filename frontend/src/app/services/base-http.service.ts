import { HttpParams } from '@angular/common/http';


export abstract class BaseHttpService {
    protected toParams(obj?: Record<string, string | number | boolean | null | undefined>): HttpParams | undefined {
        if (!obj) return undefined;
        let p = new HttpParams();
        Object.entries(obj).forEach(([k, v]) => {
            if (v !== undefined && v !== null) p = p.set(k, String(v));
        });
        return p;
    }
}