import { render, type RenderOptions } from '@testing-library/react';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';
import type { ReactElement } from 'react';

interface WrapperOptions extends RenderOptions {
  routerProps?: MemoryRouterProps;
}

export function renderWithRouter(ui: ReactElement, options: WrapperOptions = {}) {
  const { routerProps, ...renderOptions } = options;
  return render(ui, {
    wrapper: ({ children }) => (
      <MemoryRouter {...routerProps}>{children}</MemoryRouter>
    ),
    ...renderOptions,
  });
}

export { render, screen, waitFor, within, act } from '@testing-library/react';
export { default as userEvent } from '@testing-library/user-event';
