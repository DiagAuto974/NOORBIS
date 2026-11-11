// ============================================================================
// NOÖRBIS — SERVICE WORKER "NETWORK ONLY" (ZÉRO CACHE)
// Sert uniquement à valider les critères d'installation PWA (raccourci bureau/mobile)
// tout en garantissant que chaque fichier est toujours chargé en direct depuis le réseau.
// ============================================================================

self.addEventListener("install", (event) => {
  // Active immédiatement la nouvelle version du Service Worker sans attendre
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  // Supprime tout cache éventuel existant et prend le contrôle immédiat
  event.waitUntil(
    caches.keys().then((cacheNames) =>
      Promise.all(cacheNames.map((cacheName) => caches.delete(cacheName)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  // Mode 100 % Network Only : aucune mise en cache, va toujours chercher sur le serveur
  event.respondWith(fetch(event.request));
});