"use client"

import * as React from "react"
import { cn } from "@/lib/utils"

const PaginationContent = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    return <div ref={ref} className={cn("flex items-center justify-center", className)} {...props} />
  },
)
PaginationContent.displayName = "PaginationContent"

const PaginationItem = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          "w-10 h-10 flex items-center justify-center rounded-md text-sm transition-colors hover:bg-secondary",
          className,
        )}
        {...props}
      />
    )
  },
)
PaginationItem.displayName = "PaginationItem"

const PaginationLink = React.forwardRef<HTMLButtonElement, React.ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "w-10 h-10 flex items-center justify-center rounded-md text-sm transition-colors hover:bg-secondary",
          className,
        )}
        {...props}
      />
    )
  },
)
PaginationLink.displayName = "PaginationLink"

const PaginationEllipsis = React.forwardRef<HTMLSpanElement, React.HTMLAttributes<HTMLSpanElement>>(
  ({ className, ...props }, ref) => {
    return (
      <span ref={ref} className={cn("h-10 flex items-center justify-center text-sm", className)} {...props}>
        ...
      </span>
    )
  },
)
PaginationEllipsis.displayName = "PaginationEllipsis"

const PaginationPrevious = React.forwardRef<HTMLButtonElement, React.ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "w-10 h-10 flex items-center justify-center rounded-md text-sm transition-colors hover:bg-secondary",
          className,
        )}
        {...props}
      />
    )
  },
)
PaginationPrevious.displayName = "PaginationPrevious"

const PaginationNext = React.forwardRef<HTMLButtonElement, React.ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          "w-10 h-10 flex items-center justify-center rounded-md text-sm transition-colors hover:bg-secondary",
          className,
        )}
        {...props}
      />
    )
  },
)
PaginationNext.displayName = "PaginationNext"

export { PaginationContent, PaginationItem, PaginationLink, PaginationEllipsis, PaginationPrevious, PaginationNext }

