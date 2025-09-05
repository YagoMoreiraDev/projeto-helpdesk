export function upsertById<T extends { id: string }>(arr: T[], item: T): T[] {
    const i = arr.findIndex(x => x.id === item.id);
    if (i === -1) return [item, ...arr];       // novo no topo
    const clone = arr.slice();
    clone[i] = { ...clone[i], ...item };
    return clone;
}
